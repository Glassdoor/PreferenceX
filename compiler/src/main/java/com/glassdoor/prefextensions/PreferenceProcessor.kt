package com.glassdoor.prefextensions

import com.glassdoor.prefextensions.annotations.Preference
import com.google.auto.common.MoreElements
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
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
        "java.lang.Float" to ClassName.bestGuess("kotlin.Float")
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
        "java.lang.Float" to "Float"
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

        val packageName = MoreElements.getPackage(elements.first()).toString()
        val fileBuilder = FileSpec.builder(packageName, "PreferenceExtensions")
        for (element in elements) {
            val key = getElementKey(element)
            val elementName = element.simpleName.toString()
            val returnType: ClassName? = kotlinMapper[element.asType().toString()]
            val preferenceType = preferenceMapper[element.asType().toString()]

            if (returnType != null && preferenceType != null) {
                val defaultValue = getDefaultValue(element, preferenceType)

                // Getter
                fileBuilder.addFunction(FunSpec.builder("get${elementName.capitalize()}")
                    .receiver(sharedPreferenceClass)
                    .returns(returnType)
                    .addStatement("return get%L(\"%L\", %L)", preferenceType, key, defaultValue)
                    .build())

                // Setter
                fileBuilder.addFunction(FunSpec.builder("put${elementName.capitalize()}")
                    .receiver(editorClass)
                    .addParameter(ParameterSpec.builder(elementName, returnType)
                        .build())
                    .addStatement("put%L(\"%L\", %L).apply()", preferenceType, key, elementName)
                    .build())
            } else {
                messager.printMessage(Diagnostic.Kind.ERROR, "Couldn't recognize for type ${element.asType()}")
                continue
            }
        }

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
            else ->  Any()
        }
    }

    private fun getElementKey(element: Element): String {
        if (element.getAnnotation(Preference::class.java).key.isNotEmpty()) {
            return element.getAnnotation(Preference::class.java).key
        }
        return element.simpleName.toString()
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Preference::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

}