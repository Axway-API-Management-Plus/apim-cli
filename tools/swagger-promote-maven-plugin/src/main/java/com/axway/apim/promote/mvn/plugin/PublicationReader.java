package com.axway.apim.promote.mvn.plugin;

import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigFileIOException;
import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigFileInvalidFormatException;
import com.axway.apim.promote.mvn.plugin.exceptions.StageConfigNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class PublicationReader {

    /**
     * @param f
     *         path to json file
     * @param classOfT
     * @param <T>
     *
     * @return
     */
    public <T> T read(String f, Class<T> classOfT)
            throws StageConfigNotFoundException, StageConfigFileInvalidFormatException, StageConfigFileIOException {
        try {
            final Reader reader = new FileReader(f); //NOSONAR
            return read(reader, classOfT);
        } catch (JsonIOException e) {
            throw new StageConfigFileIOException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            throw new StageConfigNotFoundException(e.getMessage(), e);
        } catch (JsonSyntaxException e) {
            throw new StageConfigFileInvalidFormatException(e.getMessage(), e);
        }
    }

    /**
     * @param r
     *         The reader
     * @param classOfT
     * @param <T>
     *
     * @return
     * @throws JsonSyntaxException
     * @throws JsonIOException
     */
    public <T> T read(Reader r, Class<T> classOfT) throws JsonSyntaxException, JsonIOException {
        final JsonReader jsonReader = new Gson().newJsonReader(r);
        final T parsedObject = new Gson().fromJson(jsonReader, classOfT);
        return parsedObject;
    }

}
