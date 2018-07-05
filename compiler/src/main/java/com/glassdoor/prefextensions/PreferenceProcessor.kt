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

import com.glassdoor.prefextensions.annotations.Preference
import com.glassdoor.prefextensions.annotations.PreferenceFile
import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@AutoService(Processor::class)
class PreferenceProcessor: AbstractProcessor() {

    lateinit var elementUtils: Elements
    lateinit var typeUtils: Types
    private lateinit var messager: Messager
    private lateinit var options: Map<String, String>
    private lateinit var outputDir: File

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

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elementUtils = processingEnv.elementUtils
        typeUtils = processingEnv.typeUtils
        messager = processingEnv.messager
        options = processingEnv.options

        outputDir = processingEnv.options["kapt.kotlin.generated"]
            ?.replace("kaptKotlin", "kapt")
            ?.let(::File)
                ?: throw IllegalStateException(
            "No kapt.kotlin.generated option provided")
    }

    override fun process(set: MutableSet<out TypeElement>, roundedEnv: RoundEnvironment): Boolean {

        val elements = roundedEnv.getElementsAnnotatedWith(Preference::class.java)
        if (elements.isEmpty()) {
            return false
        }
        val fileElements = roundedEnv.getElementsAnnotatedWith(PreferenceFile::class.java)
        val wrapperElement = fileElements.first()

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

                // Getter
                fileBuilder.addFunction(FunSpec.builder("get${elementName.capitalize()}")
                    .receiver(sharedPreferenceClass)
                    .returns(returnType)
                    .addParameter(ParameterSpec.builder("defaultValue", returnType)
                        .defaultValue("%L", defaultValue)
                        .build())
                    .addStatement("return get%L(\"%L\", %L)", preferenceType, key, "defaultValue")
                    .build())

                // Setter
                fileBuilder.addFunction(FunSpec.builder("put${elementName.capitalize()}")
                    .receiver(editorClass)
                    .addParameter(ParameterSpec.builder(elementName, returnType)
                        .build())
                    .addStatement("put%L(\"%L\", %L).apply()", preferenceType, key, elementName)
                    .build())

                /**
                val receiver = getClassName(wrapperElement)

                // Getter
                wrapperBuilder.addFunction(FunSpec.builder("get${elementName.capitalize()}")
                    .receiver(receiver)
                    .returns(returnType)
                    .addParameter(ParameterSpec.builder("defaultValue", returnType)
                        .defaultValue("%L", defaultValue)
                        .build())
                    .addStatement("return %L.get%L(\"%L\", %L)", wrapperElement.simpleName.toString(), preferenceType, key, "defaultValue")
                    .build())

                // Setter
                wrapperBuilder.addFunction(FunSpec.builder("put${elementName.capitalize()}")
                    .receiver(receiver)
                    .addParameter(ParameterSpec.builder(elementName, returnType)
                        .build())
                    .addStatement("%L.edit().put%L(\"%L\", %L).apply()", wrapperElement.simpleName.toString(), preferenceType, key, elementName)
                    .build())
                **/
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Couldn't recognize for type ${element.asType()}")
                continue
            }
        }

        wrapperBuilder.build().writeTo(outputDir)
        fileBuilder.build().writeTo(outputDir)
        return true
    }

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

    private fun getElementKey(element: Element): String {
        if (element.getAnnotation(Preference::class.java).key.isNotEmpty()) {
            return element.getAnnotation(Preference::class.java).key
        }
        return element.simpleName.toString()
    }

    private fun getClassName(element: Element): ClassName {
        val classname = element.enclosingElement.simpleName.toString()
        val packageName: String
        var enclosing = element
        while (enclosing.kind != ElementKind.PACKAGE) {
            enclosing = enclosing.enclosingElement
        }
        val packageElement = enclosing as PackageElement
        packageName = packageElement.toString()

        return ClassName.bestGuess("$packageName.$classname")
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Preference::class.java.canonicalName, PreferenceFile::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

}