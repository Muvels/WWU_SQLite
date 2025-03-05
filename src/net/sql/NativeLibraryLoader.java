package net.sql;

public class NativeLibraryLoader {

	public static boolean isGlibc() {
	    // If /etc/alpine-release exists, assume it's Alpine (musl), otherwise assume glibc.
	    return !new java.io.File("/etc/alpine-release").exists();
	}
	
	public static String getLibraryResourcePath() {
		String osName = System.getProperty("os.name").toLowerCase();
		String arch = System.getProperty("os.arch").toLowerCase();
		String libraryResourcePath;
	
		if (osName.contains("win")) {
			// Windows native library
			libraryResourcePath = "/c-side/libsqlite_native.dll";
		} else if (osName.contains("mac")) {
			// macOS native library: Unterscheide zwischen ARM und Intel
			if (arch.equals("aarch64") || arch.contains("arm")) {
				// macOS auf Apple Silicon (ARM)
				libraryResourcePath = "/c-side/libsqlite_native.dylib";
			} else {
				// macOS auf Intel Chips
				libraryResourcePath = "/c-side/libsqlite_native_x64.dylib";
			}
		} else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
			// Linux/Unix native library
			String libName = "libsqlite_native";
	
			// Verwende eine Dateiprüfung, um zu entscheiden, ob es sich um glibc handelt 
			// (Annahme: glibc, wenn /etc/alpine-release nicht vorhanden ist)
			if (!isGlibc()) {
				// Wahrscheinlich ein musl-basiertes System (z.B. Alpine)
				libName += "_musl";
			}
	
			// Architektur-spezifischer Suffix
			switch (arch) {
				case "x86_64":
				case "amd64":
					libName += "_x64";
					break;
				case "aarch64":
					libName += "_arm";
					break;
				default:
					if (arch.contains("arm")) {
						libName += "_arm";
					}
					break;
			}
	
			libraryResourcePath = "/c-side/" + libName + ".so";
		} else {
			throw new UnsupportedOperationException("Unsupported operating system: " + osName);
		}
	
		return libraryResourcePath;
	}
	


}

