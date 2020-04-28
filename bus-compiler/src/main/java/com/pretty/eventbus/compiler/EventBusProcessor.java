package com.pretty.eventbus.compiler;

import com.google.auto.service.AutoService;
import com.pretty.eventbus.anno.Subscribe;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.pretty.eventbus.anno.Subscribe"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EventBusProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;//打印日志的类

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnv.getFiler();
        messager = processingEnvironment.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, "=====init=====");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "=====process=====");

        MethodSpec.Builder registerEvent = MethodSpec.methodBuilder("registerEvent")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(void.class);

        for (Element element : roundEnv.getElementsAnnotatedWith(Subscribe.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement e = (ExecutableElement) element;

                Subscribe annotation = e.getAnnotation(Subscribe.class);

                String tag = annotation.tag();
                String className = getClassName(e);
                String funName = e.getSimpleName().toString();

                if (!e.getModifiers().contains(Modifier.PUBLIC)) {
                    throw new RuntimeException("The method register to XBus should be public! error method: " + className + "#" + funName);
                }

                TypeName paramType = getParamType(e);
                String paramName = getParamName(e);
                String threadMode = annotation.threadMode().toString();
                boolean sticky = annotation.sticky();
                int priority = annotation.priority();

                registerEvent.addStatement("$T.getInstance().registerBus($S, $S, $S, $S, $S, $L, $S, $L)",
                        ClassName.get("com.pretty.eventbus.core", "BusImpl"),
                        tag, className, funName, paramType == null ? "" : paramType.toString(), paramName,
                        sticky, threadMode, priority
                );

                checkSubscriberMethod(new MethodInfo(tag, className, funName, paramType, sticky));
            } else {
                throw new RuntimeException("The Subscribe annotation can only use on method!");
            }
        }

        TypeSpec registerImpl = TypeSpec.classBuilder("BusRegisterImpl")
                .addSuperinterface(ClassName.bestGuess("com.pretty.eventbus.core.IBusRegister"))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(registerEvent.build())
                .addJavadoc("本类由注解自动生成，请勿修改!\n")
                .build();

        try {
            // build com.pretty.eventbus.BusRegisterImpl.java
            JavaFile javaFile = JavaFile.builder("com.pretty.eventbus", registerImpl)
                    .addFileComment(" This codes are generated automatically. Do not modify!")
                    .build();
            // write to file
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        generateBusManager();

        return false;
    }

    private void generateBusManager() {
        TypeSpec.Builder busManager = TypeSpec.classBuilder("BusManager")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc("本类由注解自动生成，请勿修改!\n")
                .addJavadoc("用于生成发送消息的代码，方便追踪消息订阅的类和方法!\n");

        if (methodMap.size() > 0) {
            for (Map.Entry<String, List<MethodInfo>> me : methodMap.entrySet()) {
                busManager.addMethod(generatePostMethod(me.getValue()));
            }
        }

        // 生成BusManager
        JavaFile busManagerFile = JavaFile.builder("com.pretty.eventbus", busManager.build())
                .addFileComment(" This codes are generated automatically. Do not modify!")
                .build();

        try {
            busManagerFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, List<MethodInfo>> methodMap = new HashMap<>();

    private void checkSubscriberMethod(MethodInfo methodInfo) {
        // 检查相同tag的订阅方法参数是否一样
        List<MethodInfo> infoList = methodMap.get(methodInfo.tag);
        if (infoList == null) {
            infoList = new ArrayList<>();
            methodMap.put(methodInfo.tag, infoList);
        }
        if (infoList.size() == 0) {
            infoList.add(methodInfo);
        } else {
            MethodInfo info = infoList.get(0);
            if (info.hasParam() != methodInfo.hasParam()
                    || (info.hasParam() && methodInfo.hasParam() && !info.paramType.equals(methodInfo.paramType))
            ) {
                throw new RuntimeException("XBus: 订阅相同tag的多个方法的参数个数或者参数类型不同, tag = " + methodInfo.tag);
            }
            infoList.add(methodInfo);
        }
    }

    private MethodSpec generatePostMethod(List<MethodInfo> infoList) {
        MethodInfo methodInfo = infoList.get(0);

        String methodName = methodInfo.sticky ? "postSticky" : "post";
        ClassName bus = ClassName.bestGuess("com.pretty.eventbus.core.XBus");

        MethodSpec.Builder postMethod = MethodSpec.methodBuilder("postTo_" + methodInfo.tag)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class);

        if (methodInfo.hasParam()) {
            postMethod.addParameter(methodInfo.paramType, "arg")
                    .addStatement("$T.$N($S, $L)", bus, methodName, methodInfo.tag, "arg");
        } else {
            postMethod.addStatement("$T.$N($S)", bus, methodName, methodInfo.tag);
        }

        for (MethodInfo info : infoList) {
            postMethod.addJavadoc("订阅的方法：{@link $L#$L}\n", info.className, info.funName);
        }

        return postMethod.build();
    }

    private String getClassName(ExecutableElement e) {
        Element parent = e.getEnclosingElement();
        if (parent instanceof TypeElement) {
            return ((TypeElement) parent).getQualifiedName().toString();
        } else {
            return e.asType().toString();
        }
    }

    private String getParamName(ExecutableElement e) {
        List<? extends VariableElement> parameters = e.getParameters();
        if (parameters == null || parameters.size() == 0) {
            return "";
        } else if (parameters.size() > 1) {
            throw new RuntimeException("Method register to XBus can have only one parameter!");
        } else {
            return parameters.get(0).getSimpleName().toString();
        }
    }

    private TypeName getParamType(ExecutableElement e) {
        List<? extends VariableElement> parameters = e.getParameters();
        if (parameters == null || parameters.size() == 0) {
            return null;
        } else if (parameters.size() > 1) {
            throw new RuntimeException("Method register to XBus can have only one parameter!");
        } else {
            return ClassName.get(parameters.get(0).asType());
        }
    }
}
