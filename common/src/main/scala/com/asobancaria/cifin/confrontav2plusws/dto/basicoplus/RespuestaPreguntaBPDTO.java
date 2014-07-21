/**
 * RespuestaPreguntaBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class RespuestaPreguntaBPDTO  implements java.io.Serializable {
    private int secuenciaPregunta;

    private int secuenciaRespuesta;

    public RespuestaPreguntaBPDTO() {
    }

    public RespuestaPreguntaBPDTO(
           int secuenciaPregunta,
           int secuenciaRespuesta) {
           this.secuenciaPregunta = secuenciaPregunta;
           this.secuenciaRespuesta = secuenciaRespuesta;
    }


    /**
     * Gets the secuenciaPregunta value for this RespuestaPreguntaBPDTO.
     * 
     * @return secuenciaPregunta
     */
    public int getSecuenciaPregunta() {
        return secuenciaPregunta;
    }


    /**
     * Sets the secuenciaPregunta value for this RespuestaPreguntaBPDTO.
     * 
     * @param secuenciaPregunta
     */
    public void setSecuenciaPregunta(int secuenciaPregunta) {
        this.secuenciaPregunta = secuenciaPregunta;
    }


    /**
     * Gets the secuenciaRespuesta value for this RespuestaPreguntaBPDTO.
     * 
     * @return secuenciaRespuesta
     */
    public int getSecuenciaRespuesta() {
        return secuenciaRespuesta;
    }


    /**
     * Sets the secuenciaRespuesta value for this RespuestaPreguntaBPDTO.
     * 
     * @param secuenciaRespuesta
     */
    public void setSecuenciaRespuesta(int secuenciaRespuesta) {
        this.secuenciaRespuesta = secuenciaRespuesta;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof RespuestaPreguntaBPDTO)) return false;
        RespuestaPreguntaBPDTO other = (RespuestaPreguntaBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.secuenciaPregunta == other.getSecuenciaPregunta() &&
            this.secuenciaRespuesta == other.getSecuenciaRespuesta();
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getSecuenciaPregunta();
        _hashCode += getSecuenciaRespuesta();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RespuestaPreguntaBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaPreguntaBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaPregunta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaRespuesta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaRespuesta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
