/*
 * Copyright (c) 2018 Shaishav Gandhi
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions
 *  and limitations under the License.
 */

package com.glassdoor.prefextensions

import com.glassdoor.prefextensions.PreferenceUtils.getClassName
import com.glassdoor.prefextensions.annotations.Preference
import com.google.auto.common.MoreElements
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.tools.Diagnostic

class PreferencesExtensionWriter(
    private val preferenceFileMap: Map<String, Element>,
    private val preferenceClassMap: Map<ClassName, Element>,
    private val messager: Messager
) {

    /**
     * Java type -> Kotlin type mapper.
     * When we're using annotation processing, the type of the
     * elements still come as Java types. Since we're generating
     * Kotlin extensions, this isn't ideal. Fortunately, we're
     * dealing with a limited set of types with SharedPreferences,
     * so we can map them by hand.
     */
    private val kotlinMapper = hashMapOf(
        "java.lang.String" to ClassName.bestGuess("kotlin.String"),
        "java.lang.Long" to ClassName.bestGuess("kotlin.Long"),
        "long" to ClassName.bestGuess("kotlin.Long"),
        "int" to ClassName.bestGuess("kotlin.Int"),
        "java.lang.Integer" to ClassName.bestGuess("kotlin.Int"),
        "boolean" to ClassName.bestGuess("kotlin.Boolean"),
        "java.lang.Boolean" to ClassName.bestGuess("kotlin.Boolean"),
        "float" to ClassName.bestGuess("kotlin.Float"),
        "java.lang.Float" to ClassName.bestGuess("kotlin.Float"),
        "java.util.Set<java.lang.String>" to ParameterizedTypeName.get(ClassName.bestGuess("kotlin.collections.Set"), ClassName.bestGuess("kotlin.String"))
    )

    /**
     * Mapper to map the Java type with it's corresponding
     * SharedPreference method. For example, a String
     * annotated with @Preference would mean that we would use
     * `getString()` and `putString()` for our extensions.
     */
    private val preferenceMapper = hashMapOf(
        "java.lang.String" to "String",
        "java.lang.Long" to "Long",
        "long" to "Long",
        "int" to "Int",
        "java.lang.Integer" to "Int",
        "boolean" to "Boolean",
        "java.lang.Boolean" to "Boolean",
        "float" to "Float",
        "java.lang.Float" to "Float",
        "java.util.Set<java.lang.String>" to "StringSet"
    )

    private val sharedPreferenceClass = ClassName.bestGuess("android.content.SharedPreferences")
    private val editorClass = ClassName.bestGuess("android.content.SharedPreferences.Editor")


    fun writeExtensions(elements: Set<Element>, outputDir: File) {
        val packageName = MoreElements.getPackage(elements.first()).toString()
        val fileBuilder = FileSpec.builder(packageName, "PreferenceExtensions")
        val wrapperBuilder = FileSpec.builder(packageName, "PreferenceWrapperExtensions")
        for (element in elements) {
            val key = getElementKey(element)
            val elementName = element.simpleName.toString()
            val returnType: TypeName? = kotlinMapper[element.asType().toString()]
            val preferenceType = preferenceMapper[element.asType().toString()]

            if (returnType != null && preferenceType != null) {
                val defaultValue = getDefaultValue(element, preferenceType)

                // Preference Getter
                fileBuilder.addFunction(getter(sharedPreferenceClass, returnType, defaultValue, key, preferenceType, elementName))

                // Preference Setter
                fileBuilder.addFunction(
                    setter(editorClass, returnType, key, preferenceType, elementName)
                )

                // Check if the declared @Preference has a `file` also specified.
                // This means that the user is using it in their own custom
                // wrapper and wishes that we create extensions on the wrapper
                // as well.
                val file = element.getAnnotation(Preference::class.java).file

                if (file.isNotEmpty()) {
                    // User specified the file in @PreferenceFile.
                    // We try and map the file in @Preference and
                    // @PreferenceFile

                    preferenceFileMap[file]?.let { wrapperElement ->
                        val receiver = getClassName(wrapperElement)

                        // Wrapper Getter
                        wrapperBuilder.addFunction(
                            getter(receiver, returnType, defaultValue, key, preferenceType, elementName,
                                true, wrapperElement.simpleName.toString())
                        )

                        // Wrapper Setter
                        wrapperBuilder.addFunction(
                            setter(receiver, returnType, key, preferenceType, elementName, true,
                                wrapperElement.simpleName.toString())
                        )
                    }
                } else if (preferenceFileMap.isEmpty()) {
                    // User hasn't specified any `file` in @PreferenceFile
                    // We fallback to the class and see if they match.

                    val receiver = getClassName(element)

                    preferenceClassMap[receiver]?.let { wrapperElement ->

                        // Wrapper Getter
                        wrapperBuilder.addFunction(
                            getter(receiver, returnType, defaultValue, key, preferenceType, elementName,
                                true, wrapperElement.simpleName.toString())
                        )

                        // Wrapper Setter
                        wrapperBuilder.addFunction(
                            setter(receiver, returnType, key, preferenceType, elementName, true,
                                wrapperElement.simpleName.toString())
                        )
                    }
                }
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Couldn't recognize for type ${element.asType()}")
                continue
            }
        }

        wrapperBuilder.build().writeTo(outputDir)
        fileBuilder.build().writeTo(outputDir)
    }

    /**
     * Returns the function that will generate the getter
     * for SharedPreferences as well as any wrappers.
     *
     * @param receiver
     * @param returnType
     * @param defaultValue
     * @param key
     * @param preferenceType
     * @param elementName
     * @param forWrapper
     * @param wrapperName
     *
     * @return [FunSpec]
     */
    private fun getter(receiver: ClassName, returnType: TypeName,
               defaultValue: Any, key: String, preferenceType: String,
               elementName: String, forWrapper: Boolean = false, wrapperName: String = ""): FunSpec {
        val builder =  FunSpec.builder("get${elementName.capitalize()}")
            .receiver(receiver)
            .returns(returnType)
            .addParameter(
                ParameterSpec.builder("defaultValue", returnType)
                    .defaultValue("%L", defaultValue)
                    .build())
        if (forWrapper) {
            builder.addStatement("return %L.get%L(\"%L\", %L)", wrapperName, preferenceType, key, "defaultValue")
        } else {
            builder.addStatement("return get%L(\"%L\", %L)", preferenceType, key, "defaultValue")
        }
        return builder.build()
    }

    /**
     * Returns the generated code for the setter on
     * SharedPreferences.Editor class or any of it's wrappers.
     *
     * @param receiver
     * @param returnType
     * @param key
     * @param preferenceType
     * @param elementName
     * @param forWrapper
     * @param wrapperName
     *
     * @return [FunSpec]
     */
    private fun setter(receiver: ClassName, returnType: TypeName,
               key: String, preferenceType: String, elementName: String,
               forWrapper: Boolean = false, wrapperName: String = ""): FunSpec {
        val builder =  FunSpec.builder("put${elementName.capitalize()}")
            .receiver(receiver)
            .addParameter(
                ParameterSpec.builder(elementName, returnType)
                    .build())
        if (forWrapper) {
            builder.addStatement("%L.edit().put%L(\"%L\", %L).apply()", wrapperName, preferenceType, key, elementName)
        } else {
            builder.addStatement("put%L(\"%L\", %L).apply()", preferenceType, key, elementName)
        }
        return builder.build()
    }


    /**
     * Get default value of a given element annotated with
     * @[Preference]. Checks if the user has given a default
     * value with property like `[Preference.defaultInt]` or
     * `[Preference.defaultString]` and returns that.
     * Otherwise, just returns the default-default value
     * specified in @[Preference].
     *
     * @param element annotated with @[Preference]
     * @param preferenceType String/Int/Long etc
     *
     * @return [Any]
     */
    private fun getDefaultValue(element: Element, preferenceType: String): Any {
        val prefAnnotation = element.getAnnotation(Preference::class.java)
        return when (preferenceType) {
            "String" -> "\"${prefAnnotation.defaultString}\""
            "Long" -> prefAnnotation.defaultLong
            "Int" -> prefAnnotation.defaultInt
            "Float" -> "${prefAnnotation.defaultFloat}f"
            "Boolean" -> prefAnnotation.defaultBoolean
            "StringSet" -> "emptySet<String>()"
            else ->  Any()
        }
    }

    /**
     * Returns the key of the element annotated with @[Preference].
     * Checks if @[Preference.key] is specified and returns that
     * value, otherwise just returns the name of the element.
     *
     * @param element annotated with @[Preference]
     *
     * @return [String]
     */
    private fun getElementKey(element: Element): String {
        if (element.getAnnotation(Preference::class.java).key.isNotEmpty()) {
            return element.getAnnotation(Preference::class.java).key
        }
        return element.simpleName.toString()
    }

}