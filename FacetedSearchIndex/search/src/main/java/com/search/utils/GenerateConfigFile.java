package com.search.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.search.types.Config;
import com.search.types.Dataset;
import com.search.types.DatasetType;
import com.search.types.Index;
import com.search.types.Ontology;
import com.search.types.Package;

public class GenerateConfigFile {
    public GenerateConfigFile(String filePath, String ontologyName, String ontologyEndpoint, String datasetName,
            DatasetType datasetType,
            String datasetEndpoint, String configsFolderPath, HashSet<String> indiciesToCreateAtStartup) {
        try {
            List<Index> indices = new ArrayList<>();
            List<Package> packages = new ArrayList<>();

            for (File file : new File(configsFolderPath).listFiles()) {
                if (file.isFile()) {
                    String[] splitted = file.getName().split("/");
                    indices.add(new Index(splitted[splitted.length - 1].split(".json")[0], file.getPath(), true));
                }
            }

            packages.add(new Package(new Ontology(ontologyName, ontologyEndpoint),
                    new Dataset(datasetName, datasetType, datasetEndpoint), indices));

            Config config = new Config(packages, indiciesToCreateAtStartup);

            File file = new File(filePath);
            file.createNewFile();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
