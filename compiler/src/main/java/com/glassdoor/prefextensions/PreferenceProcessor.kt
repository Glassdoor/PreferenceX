/*
 * Copyright (c) 2018 Glassdoor, Inc.
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

    private lateinit var elementUtils: Elements
    private lateinit var typeUtils: Types
    private lateinit var messager: Messager
    private lateinit var options: Map<String, String>
    private lateinit var outputDir: File

    private val preferenceFileMap = mutableMapOf<String, Element>()
    private val preferenceClassMap = mutableMapOf<ClassName, Element>()

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

        for (file in fileElements) {
            if (file.getAnnotation(PreferenceFile::class.java).fileName.isNotEmpty()) {
                messager.printMessage(Diagnostic.Kind.WARNING, "Got in")
                val fileName = file.getAnnotation(PreferenceFile::class.java).fileName
                preferenceFileMap[fileName] = file
            } else {
                val className = getClassName(file)
                preferenceClassMap[className] = file
            }
        }

        val writer = PreferencesExtensionWriter(preferenceFileMap, preferenceClassMap, messager)
        writer.writeExtensions(elements, outputDir)
        return true
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return setOf(Preference::class.java.canonicalName, PreferenceFile::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

}