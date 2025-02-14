package opg.p2vman.moduleloader.module;

import com.google.gson.Gson;
import m.dict.Lock;
import org.objectweb.asm.*;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleMannager {
    public static final FilenameFilter MODULEFILTER = (File dir, String name) -> name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".ear");
    private final MethodHandles.Lookup lookup;
    private final Gson GSON;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, String> classNameMap = new HashMap<>();

    public ModuleMannager() throws IllegalAccessException {
        lookup = MethodHandles.privateLookupIn(Lock.class, MethodHandles.lookup());
        GSON = new Gson();
    }

    public List<JavaModule> loadModules(File dir) {
        List<JavaModule> modules = new ArrayList<>();
        for (File file : Objects.requireNonNull(dir.listFiles(MODULEFILTER))) {
            try {
                modules.add(loadModule(file).get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return modules;
    }

    public Supplier<JavaModule> loadModule(File plugin) {
        synchronized (lookup) {
            classNameMap.clear();
            List<Class<?>> classes = new ArrayList<>();
            Optional<JavaModule> module = Optional.empty();

            try (JarFile jarFile = new JarFile(plugin)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().endsWith(".class")) {
                        try (InputStream is = jarFile.getInputStream(entry)) {
                            Class<?> cls = transformClass(is.readAllBytes());
                            classes.add(cls);
                            System.out.println("Loaded: " + cls.getName());
                        } catch (Exception e) {
                            throw new AssertionError(e);
                        }
                    }
                }

                try (InputStream is = jarFile.getInputStream(jarFile.getEntry("module.json"))) {
                    ModuleMeta meta = GSON.fromJson(new InputStreamReader(is), ModuleMeta.class);
                    ModuleContainer container = new ModuleContainer(meta, plugin);
                    ModuleResourceMannager resourceMannager = new ModuleResourceMannager(container);
                    ModuleLogger logger = new ModuleLogger(container);

                    String newMainClass = classNameMap.get(container.meta.main_class.replace('.', '/')).replace('/', '.');
                    JavaModule module1 = (JavaModule) lookup.findConstructor(lookup.findClass(newMainClass), MethodType.methodType(void.class)).invoke();
                    module1.init(container, resourceMannager, logger);

                    module = Optional.of(module1);
                } catch (Throwable e) {
                    throw new AssertionError(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final JavaModule module1 = module.orElseThrow();
            return () -> {
                module1.onLoad();
                return module1;
            };
        }
    }

    private final Map<String, Class<?>> classCache = new HashMap<>();

    private Class<?> transformClass(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        String oldName = reader.getClassName();
        String newName = "m/dict/" + generateRandomName();

        while (classCache.containsKey(newName)) {
            newName = "m/dict/" + generateRandomName();
        }

        classNameMap.put(oldName, newName);

        final String finalnewName = newName;
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, finalnewName, signature, superName, interfaces);

                AnnotationVisitor annotationVisitor = super.visitAnnotation("Lopg/eptalist/moduleloader/api/RemapedClass;", true);
                annotationVisitor.visit("value", oldName);
                annotationVisitor.visitEnd();
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                return super.visitField(access, name, updateDescriptor(descriptor), signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, updateDescriptor(descriptor), signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                        super.visitFieldInsn(opcode, remapClass(owner), name, updateDescriptor(descriptor));
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                        super.visitMethodInsn(opcode, remapClass(owner), name, updateDescriptor(descriptor), isInterface);
                    }

                    @Override
                    public void visitTypeInsn(int opcode, String type) {
                        super.visitTypeInsn(opcode, remapClass(type));
                    }

                    @Override
                    public void visitLdcInsn(Object value) {
                        if (value instanceof String && classNameMap.containsKey(value)) {
                            value = classNameMap.get(value);
                        }
                        super.visitLdcInsn(value);
                    }
                };
            }
        }, 0);

        byte[] transformedBytes = writer.toByteArray();

        if (!classCache.containsKey(newName)) {
            try {
                classCache.put(newName, lookup.defineClass(transformedBytes));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        return classCache.get(newName);
    }

    private String remapClass(String className) {
        return classNameMap.getOrDefault(className, className);
    }

    private String updateDescriptor(String descriptor) {
        if (descriptor == null) return null;
        for (Map.Entry<String, String> entry : classNameMap.entrySet()) {
            descriptor = descriptor.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
        }
        return descriptor;
    }

    private String generateRandomName() {
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
