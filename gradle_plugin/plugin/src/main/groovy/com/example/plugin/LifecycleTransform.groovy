package com.example.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.Opcodes.*

class LifecycleTransform extends Transform {
    public static final String SCAN_INTERFACE = 'com/example/lifecycle/IApplication'
    public static final String SCAN_MANAGER = 'com/example/lifecycle/AppLifecycleManager'
    public static final HashSet<String> LIST = new HashSet<>()
    /**
     * com/example/lifecycle/AppLifecycleManager所在的jar包
     */
    public static File manager_jar

    @Override
    String getName() {
        return "LifecycleTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //搜索范围限于当前工程和子工程
        return [QualifiedContent.Scope.PROJECT, QualifiedContent.Scope.SUB_PROJECTS]
    }

    @Override
    Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return super.getReferencedScopes()
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)

        inputs.each { input ->
            //扫描jar包的class文件, 组件作为依赖时以jar包的方式
            input.jarInputs.each { jarInput ->
                println("input jars: ${jarInput.file}")
                JarFile jar = new JarFile(jarInput.file)
                def entries = jar.entries()
                def dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                while (entries.hasMoreElements()) {
                    def element = entries.nextElement()
                    if (!element.name.endsWith(".class")) continue

                    if (element.name.endsWith("${SCAN_MANAGER}.class")) {
                        manager_jar = dest
                    } else {
                        def stream = jar.getInputStream(element)
                        ClassReader cr = new ClassReader(stream)
                        cr.accept(new SearchImplAdapter(), 0)
                        stream.close()
                    }
                }

                FileUtils.copyFile(jarInput.file, dest)
            }

            // 扫描目录下的class文件
            input.directoryInputs.each { dirInput ->
                println("input directory: ${dirInput.file}")
                //Transform扫描的class文件是输入文件(input)，处理完成后需要将输入文件拷贝到一个输出目录(output)，
                //之后class打包成dex文件时，直接采用output下的class。如果没有输出， 生成dex时也就不会将
                //相应的class打包进去
                def dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(dirInput.file, dest)
            }
        }

        println """\
            ${SCAN_INTERFACE} implementations:
                ${LIST}
            ${SCAN_MANAGER} at:
                ${manager_jar}
        """
        if (manager_jar == null || !manager_jar.exists() || LIST.isEmpty()) {
            return
        }

        // 遍历含有AppLifecycleManager的jar包
        JarFile jar = new JarFile(manager_jar)
        def optJar = new File(manager_jar.parent, "${manager_jar.name}.opt")
        if (optJar.exists())
            optJar.delete()
        def output = new JarOutputStream(new FileOutputStream(optJar))
        def entries = jar.entries()
        while (entries.hasMoreElements()) {
            def element = entries.nextElement()
            output.putNextEntry(new ZipEntry(element.name))
            def input = jar.getInputStream(element)
            if (!element.name.endsWith("${SCAN_MANAGER}.class")) {
                output.write(IOUtils.toByteArray(input))
            } else {
                //找到AppLifecycleManager类, 进行字节码修改
                ClassReader cr = new ClassReader(input)
                ClassWriter cw = new ClassWriter(cr, 0)
                cr.accept(new TransformClassAdapter(ASM6, cw), 0)
                output.write(cw.toByteArray())
            }
            input.close()
            output.closeEntry()
        }
        output.close()
        jar.close()

        if (manager_jar.exists()) {
            manager_jar.delete()
        }
        optJar.renameTo(manager_jar)
    }

    static class SearchImplAdapter extends ClassVisitor {
        SearchImplAdapter() {
            super(ASM6)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (interfaces != null) {
                interfaces.each {
                    if (it == SCAN_INTERFACE) {
                        println "--> class $name extends $superName implements ${Arrays.toString(interfaces)}"
                        LIST.add(name)
                    }
                }
            }
            super.visit(version, access, name, signature, superName, interfaces)
        }
    }

    static class TransformClassAdapter extends ClassVisitor {

        TransformClassAdapter(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            // 通过asm生成全新的static块, 这里返回null将static先移除(代码中有静态初始化代码,必有static{})
            def mv = cv.visitMethod(access, name, desc, signature, exceptions)
            if (name == "<clinit>") {
                mv = new StaticMethodAdapter(ASM6, mv)
            }
            return mv
        }

    }

    static class StaticMethodAdapter extends MethodVisitor {

        StaticMethodAdapter(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitInsn(int opcode) {
            if (opcode >= IRETURN && opcode <= RETURN) {
                LIST.each { name ->
                    mv.visitLdcInsn(name.replace("/", "."))
                    mv.visitMethodInsn(INVOKESTATIC, SCAN_MANAGER, "register", "(Ljava/lang/String;)V", false)
                }
                mv.visitMethodInsn(INVOKESTATIC, SCAN_MANAGER, "init", "()V", false)
            }
            super.visitInsn(opcode)
        }
    }
}