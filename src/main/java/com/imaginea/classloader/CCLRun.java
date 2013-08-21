package com.imaginea.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class CCLRun extends Thread {
	private static Class<?> mainClass;

	public void run() {
		try {
			CompilingClassLoader ccl = new CompilingClassLoader();
			Class<?> clas = ccl.loadClass(mainClass.getName());
			Class<?> mainArgType[] = { (new String[0]).getClass() };
			Method main = clas.getMethod("main", mainArgType);
			Object argsArray[] = { (Object) null };
			main.invoke(null, argsArray);
			System.out.println("completed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception {

		/*
		 * while (true) { Thread myThread = new CCLRun(); Thread.sleep(5000);
		 * System.out.println("started"); myThread.start(); Thread.sleep(5000);
		 */
		// Reflections reflections = new
		// Reflections("com.imaginea.classloader");
		Class<?>[] classes = getClasses("",
				"/home/gangaraju/jar/AwtExample.jar");
		for (int i = 0; i < classes.length; i++) {
			Class<?> clas = Class.forName(classes[i].getName());
			Method[] methods = clas.getDeclaredMethods();
			for (int j = 0; j < methods.length; j++) {
				if (methods[j].getName().contains("main")) {
					CCLRun.mainClass = methods[j].getDeclaringClass();
					System.out.println("Main Method is: "
							+ methods[j].getDeclaringClass());
				}
			}
		}
		System.out.println("The Available Classes are:");
		for (int i = 0; i < classes.length; i++) {
			System.out.println(classes[i].toString());
		}
		Thread myThread = new CCLRun();
		Thread.sleep(5000);
		System.out.println("started");
		myThread.start();
		/* System.out.println("restarting..."); myThread.stop(); } */
	}

	@SuppressWarnings("unused")
	private static Class<?>[] getClasses(String packageName)
			throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static List<Class<?>> findClasses(File directory, String packageName)
			throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6)));
			}
		}
		return classes;
	}

	public static Class<?>[] getClasses(String packageName, String jarName)
			throws ClassNotFoundException {
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

		packageName = packageName.replaceAll("\\.", "/");
		File f = new File(jarName);
		if (f.exists()) {
			try {
				JarInputStream jarFile = new JarInputStream(
						new FileInputStream(jarName));
				JarEntry jarEntry;

				while (true) {
					jarEntry = jarFile.getNextJarEntry();
					if (jarEntry == null) {
						break;
					}
					if ((jarEntry.getName().startsWith(packageName))
							&& (jarEntry.getName().endsWith(".class"))) {
						classes.add(Class.forName(jarEntry.getName()
								.replaceAll("/", "\\.")
								.substring(0, jarEntry.getName().length() - 6)));
					}
				}
				jarFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return classes.toArray(new Class[classes.size()]);
		} else {
			return null;
		}
	}

}
