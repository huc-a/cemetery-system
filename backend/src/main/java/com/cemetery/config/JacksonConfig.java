package com.cemetery.config;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.*; import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
@Configuration public class JacksonConfig{
 @Bean public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer(){
  DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  return b -> b.serializers(new LocalDateTimeSerializer(fmt))
               .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(fmt)); } }
