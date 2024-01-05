package org.bukkit.plugin.java;

import com.mohistmc.banner.BannerMCStart;
import com.mohistmc.banner.BannerServer;
import com.mohistmc.banner.bukkit.nms.proxy.DelegateURLClassLoder;
import mjson.Json;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

class LibraryLoader {

    public LibraryLoader() {
    }

    @Nullable
    public ClassLoader createLoader(@NotNull PluginDescriptionFile desc) throws IOException {
        if (desc.getLibraries().isEmpty()) {
            return null;
        }
        BannerServer.LOGGER.info(BannerMCStart.I18N.get("spigot.lib.loading"), desc.getName(), desc.getLibraries().size());

        List<Dependency> dependencies = new ArrayList<>();
        for (String libraries : desc.getLibraries()) {
            String[] args = libraries.split(":");
            if (args.length > 1) {
                Dependency dependency = new Dependency(args[0], args[1], args[2], false);
                dependencies.add(dependency);
            }

        }

        List<File> libraries = new ArrayList<>();
        List<Dependency> newDependencies = new ArrayList<>();

        for (Dependency dependency : dependencies) {
            newDependencies.add(dependency);
            String group = dependency.group().replace(".", "/");
            String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());

            String pomUrl = "https://repo.maven.apache.org/maven2/%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName.replace("jar", "pom"));
            newDependencies.addAll(initDependencies0(new URL(pomUrl)));
        }

        BannerServer.LOGGER.info(BannerMCStart.I18N.get("spigot.lib.loading.extra"), desc.getName(), newDependencies.size() - desc.getLibraries().size());

        for (Dependency dependency : newDependencies) {
            String group = dependency.group().replace(".", "/");
            String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
            String mavenUrl = "https://repo.maven.apache.org/maven2/%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName);

            File file = new File(new File("libraries", "spigot-lib"), "%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName));

            if (file.exists()) {
                BannerServer.LOGGER.info(BannerMCStart.I18N.get("spigot.lib.found"), desc.getName(), file);
                libraries.add(file);
                continue;
            }
            try {
                file.getParentFile().mkdirs();

                InputStream inputStream = new URL(mavenUrl).openStream();
                ReadableByteChannel rbc = Channels.newChannel(inputStream);
                FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                fc.transferFrom(rbc, 0, Long.MAX_VALUE);
                fc.close();
                rbc.close();

                libraries.add(file);
            } catch (IOException e) {
                BannerServer.LOGGER.error(e.getMessage());
            }
        }

        List<URL> jarFiles = new ArrayList<>();
        for (File file : libraries) {
            try {
                jarFiles.add(file.toURI().toURL());
                BannerServer.LOGGER.info(BannerMCStart.I18N.get("spigot.lib.loaded"), desc.getName(), file);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        return new DelegateURLClassLoder(jarFiles.toArray(new URL[0]), getClass().getClassLoader());
    }

    public static List<Dependency> initDependencies0(URL url) throws IOException {
        List<Dependency> list = new ArrayList<>();
        for (Dependency dependency : initDependencies(url)) {
            list.add(dependency);
            if (dependency.extra()) {
                String group = dependency.group().replace(".", "/");
                String fileName = "%s-%s.jar".formatted(dependency.name(), dependency.version());
                String pomUrl = "https://repo.maven.apache.org/maven2/%s/%s/%s/%s".formatted(group, dependency.name(), dependency.version(), fileName.replace("jar", "pom"));
                if (hasUrl(pomUrl)) list.addAll(initDependencies(new URL(pomUrl)));
            }
        }
        return list;
    }

    public static List<Dependency> initDependencies(URL url) {
        List<Dependency> list = new ArrayList<>();
        Json json2Json = Json.readXml(url).at("project");
        String version = json2Json.has("parent") ? json2Json.at("parent").asString("version") : json2Json.asString("version");

        if (!json2Json.has("dependencies")) return list;
        if (!json2Json.at("dependencies").toString().startsWith("{\"dependency\"")) return list;
        Json json3Json = json2Json.at("dependencies").at("dependency");
        if (json3Json.isArray()) {
            for (Json o : json2Json.at("dependencies").asJsonList("dependency")) {
                dependency(o, list, version);
            }
        } else {
            dependency(json3Json, list, null);
        }
        return list;
    }

    public static void dependency(Json json, List<Dependency> list, String version) {
        try {
            if (json.toString().contains("groupId") && json.toString().contains("artifactId")) {
                String groupId = json.asString("groupId");
                String artifactId = json.asString("artifactId");
                if (json.toString().contains("version")) {
                    String versionAsString = json.asString("version");
                    if (versionAsString.contains("${project.version}")) {
                        Dependency dependency = new Dependency(groupId, artifactId, version, true);
                        list.add(dependency);
                    } else if (!versionAsString.contains("${")) {
                        Dependency dependency = new Dependency(groupId, artifactId, versionAsString, true);
                        list.add(dependency);
                    }
                } else {
                    if (json.has("scope") && json.asString("scope").equals("compile")) {
                        URL mavenUrl = URI.create("https://repo.maven.apache.org/maven2/%s/%s/%s".formatted(groupId.replace(".", "/"), artifactId, "maven-metadata.xml")).toURL();
                        Json compile_json2Json = Json.readXml(mavenUrl).at("metadata");
                        List<Object> v = compile_json2Json.at("versioning").at("versions").at("version").asList();
                        Dependency dependency = new Dependency(groupId, artifactId, v.get(v.size() - 1), true);
                        list.add(dependency);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static boolean hasUrl(String s) {
        try {
            URL url = new URL(s);
            url.openStream();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public record Dependency(String group, String name, Object version, boolean extra) {
    }
}
