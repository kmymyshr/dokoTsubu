package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** アプリ全体で使用する、設定済みObjectMapperを提供する。 */
public final class ObjectMapperFactory {
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
			.addModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.build();

	private ObjectMapperFactory() {
	}

	public static ObjectMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}
}
