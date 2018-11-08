package org.sunbird.media.util

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.jackson.JsonMethods
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.SerializationFeature

/**
  * @author Pradyumna
  */
object JSONUtils {

  @transient val mapper = new ObjectMapper();
  mapper.registerModule(DefaultScalaModule);
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
  mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
  mapper.setSerializationInclusion(Include.NON_NULL);

  @throws(classOf[Exception])
  def serialize(obj: AnyRef): String = {
    mapper.writeValueAsString(obj);
  }

  @throws(classOf[Exception])
  def deserialize[T: Manifest](value: String): T = mapper.readValue(value, typeReference[T]);

  private[this] def typeReference[T: Manifest] = new TypeReference[T] {
    override def getType = typeFromManifest(manifest[T])
  }


  private[this] def typeFromManifest(m: Manifest[_]): Type = {
    if (m.typeArguments.isEmpty) { m.runtimeClass }
    // $COVERAGE-OFF$Disabling scoverage as this code is impossible to test
    else new ParameterizedType {
      def getRawType = m.runtimeClass
      def getActualTypeArguments = m.typeArguments.map(typeFromManifest).toArray
      def getOwnerType = null
    }
    // $COVERAGE-ON$
  }

}