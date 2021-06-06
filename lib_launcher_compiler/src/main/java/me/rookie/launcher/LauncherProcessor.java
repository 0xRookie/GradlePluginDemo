package me.rookie.launcher;

import com.google.auto.service.AutoService;
import me.rookie.annotation.ILauncherLoader;
import me.rookie.annotation.Launcher;
import me.rookie.annotation.LauncherMeta;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("me.rookie.annotation.Launcher")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class LauncherProcessor extends AbstractProcessor {
    private Messager           mMessager;
    private Filer              mFiler;
    private Elements           mElements;
    private List<LauncherMeta> mLauncherMetaList = new ArrayList<>();
    private Map<String, String> mOptions;
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mMessager = processingEnvironment.getMessager();
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        mMessager.printMessage(Diagnostic.Kind.NOTE, "LauncherProcessor Init!!!");
        mOptions = processingEnvironment.getOptions();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String moduleName = mOptions.get("AROUTER_MODULE_NAME");
        mMessager.printMessage(Diagnostic.Kind.NOTE, "LauncherProcessor Start Process!!!");
        TypeElement typeElement = mElements.getTypeElement("me.rookie.annotation.Launcher");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(typeElement);
        for (Element element : elements) {
            mMessager.printMessage(Diagnostic.Kind.NOTE, String.format("Find Launcher class %s", element.asType().toString()));
            Launcher annotation = element.getAnnotation(Launcher.class);
            LauncherMeta launcherMeta = new LauncherMeta(
                    annotation.group(),
                    annotation.authority(),
                    annotation.route(),
                    annotation.icon(),
                    annotation.label());
            mLauncherMetaList.add(launcherMeta);
        }

        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(List.class, LauncherMeta.class);

        MethodSpec.Builder loadBuilder = MethodSpec.methodBuilder("load")
                .returns(void.class)
                .addParameter(parameterizedTypeName, "list")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);

        for (LauncherMeta launcherMeta : mLauncherMetaList) {
            loadBuilder.addStatement("list.add(new $T($S,$S,$S,$S,$S))",
                    LauncherMeta.class,
                    launcherMeta.getGroup(), launcherMeta.getAuthority(), launcherMeta.getRoute(), launcherMeta.getIcon(), launcherMeta.getLabel());
        }

        PackageElement packageElement = mElements.getPackageOf(typeElement);

        TypeSpec typeSpec = TypeSpec.classBuilder("LauncherLoaderImpl_"+moduleName)
                .addSuperinterface(ILauncherLoader.class)
                .addMethod(loadBuilder.build())
                .addModifiers(Modifier.PUBLIC)
                .build();

        mMessager.printMessage(Diagnostic.Kind.NOTE, String.format("Start to write java file %s!", typeElement.getQualifiedName().toString()));

        JavaFile javaFile = JavaFile.builder(packageElement.getQualifiedName().toString(), typeSpec).build();

        try {
            javaFile.writeTo(mFiler);
        }catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }
}