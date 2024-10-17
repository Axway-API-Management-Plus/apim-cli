package com.axway.apim.appexport.impl.jackson;

import java.io.File;

import com.axway.apim.api.model.APIQuota;
import com.axway.apim.api.model.apps.ClientAppCredential;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

public class AppExportSerializerModifier extends BeanSerializerModifier {

	File localFolder;

	public AppExportSerializerModifier(File localFolder) {
		this.localFolder = localFolder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
		if (beanDesc.getBeanClass().getSuperclass() == ClientAppCredential.class) {
			AppCredentialsSerializer appSer = new AppCredentialsSerializer((JsonSerializer<Object>) serializer);
			appSer.setExportFolder(localFolder);
			return appSer;
		} else if (beanDesc.getBeanClass() == APIQuota.class) {
			return new AppQuotaSerializer((JsonSerializer<Object>) serializer);
		}

		return super.modifySerializer(config, beanDesc, serializer);
	}

}
