package info.u_team.music_player.dependency;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.file.*;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.*;

import info.u_team.music_player.MusicPlayerMod;
import net.minecraftforge.fml.ModList;

public class DependencyManager {
	
	private static final Logger logger = LogManager.getLogger();
	
	public static final DependencyMusicPlayerClassLoader musicplayerclassloader = new DependencyMusicPlayerClassLoader();
	
	private static Path cache;
	
	private static Path embeddependencies, musicplayerdependencies;
	
	public static void construct() {
		setupCache();
		copyDependencies();
		loadDependencies();
	}
	
	private static void setupCache() {
		cache = Paths.get(System.getProperty("java.io.tmpdir"), "musicplayer-dependency-cache");
		embeddependencies = cache.resolve("embed");
		musicplayerdependencies = cache.resolve("musicplayer");
		logger.info("Creating musicplayer cache at " + cache);
		
		FileUtils.deleteQuietly(cache.toFile());
		try {
			Files.createDirectory(cache);
			Files.createDirectory(embeddependencies);
			Files.createDirectory(musicplayerdependencies);
		} catch (IOException ex) {
			logger.error("Could not create music player cache", ex);
		}
		
	}
	
	private static void copyDependencies() {
		logger.info("Try to copy dependencies to cache directory");
		String path = System.getProperty("musicplayer.dev");
		try {
			if (path != null) {
				copyDependenciesDev(path);
			} else {
				copyDependenciesFromJar();
			}
			logger.info("Finished copy of dependencies to cache");
		} catch (Exception ex) {
			logger.error("Could not copy dependencies to cache", ex);
		}
	}
	
	private static void copyDependenciesDev(String path) throws Exception {
		logger.info("Search dependencies in dev");
		
		FileUtils.copyDirectory(Paths.get(path, "musicplayer-lavaplayer/build/libs").toFile(), musicplayerdependencies.toFile());
		FileUtils.copyDirectory(Paths.get(path, "musicplayer-lavaplayer/build/dependencies").toFile(), musicplayerdependencies.toFile());
	}
	
	private static void copyDependenciesFromJar() throws Exception {
		logger.info("Search dependencies in jar");
		
		Files.walk(ModList.get().getModFileById(MusicPlayerMod.modid).getFile().findResource("/embed-dependencies")).filter(path -> path.toString().endsWith(".jar")).forEach(path -> {
			try {
				Files.copy(path, new File(embeddependencies.toFile(), path.getFileName().toString()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});
		
		Files.walk(ModList.get().getModFileById(MusicPlayerMod.modid).getFile().findResource("/dependencies")).filter(path -> path.toString().endsWith(".jar")).forEach(path -> {
			try {
				Files.copy(path, new File(musicplayerdependencies.toFile(), path.getFileName().toString()).toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		});
	}
	
	private static void loadDependencies() {
		logger.info("Load dependencies into classloader");
		
		try {
			Files.walk(embeddependencies).filter(path -> path.toString().endsWith(".jar") && path.toFile().isFile()).forEach(path -> {
				URLClassLoader systemloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				try {
					Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
					method.setAccessible(true);
					method.invoke(systemloader, path.toUri().toURL());
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			});
		} catch (Exception ex) {
			logger.error("Could not load file into classloader ", ex);
		}
		
		try {
			Files.walk(musicplayerdependencies).filter(path -> path.toString().endsWith(".jar") && path.toFile().isFile()).forEach(path -> musicplayerclassloader.addFile(path.toFile()));
		} catch (IOException ex) {
			logger.error("Could not load file into classloader ", ex);
		}
		logger.info("Dependencies have sucessfully been loaded into classloader");
	}
}