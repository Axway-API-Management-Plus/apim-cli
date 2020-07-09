package com.axway.apim.adapter.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.format.InputAccessor;
import com.fasterxml.jackson.core.format.MatchStrength;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YAMLFactoryExt extends YAMLFactory {

	private static final long serialVersionUID = 1L;

	public YAMLFactoryExt() {
	}

	public YAMLFactoryExt(ObjectCodec oc) {
		super(oc);
	}

	public YAMLFactoryExt(YAMLFactory src, ObjectCodec oc) {
		super(src, oc);
	}

	@Override
	public MatchStrength hasFormat(InputAccessor acc) throws IOException {
		MatchStrength matchStrength = super.hasFormat(acc);
		// The standard Yaml-Factory doesn't accept Yaml-Files without the optional start-marker ---
		if(matchStrength.equals(MatchStrength.INCONCLUSIVE)) {
			acc.reset();
			byte b = acc.nextByte();
			while(acc.hasMoreBytes()) {
				// Ignore newlines at the beginning of the file
				if(b == '\n' || b == '\r') {
					b = acc.nextByte();
					continue;
				}
				break;
			}
			// As we are supporting JSON and YAML only, we can reverse the check
			if (b != '[' && b != '{' && b != '<') { // This checks, that it is not JSON and not XML
				return MatchStrength.SOLID_MATCH;
			}
		}
		return matchStrength;
	}
}
