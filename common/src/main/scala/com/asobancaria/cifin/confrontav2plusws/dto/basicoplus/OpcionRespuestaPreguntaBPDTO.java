/**
 * OpcionRespuestaPreguntaBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class OpcionRespuestaPreguntaBPDTO  extends RespuestaPreguntaBPDTO  implements java.io.Serializable {
    private String textoRespuesta;

    public OpcionRespuestaPreguntaBPDTO() {
    }

    public OpcionRespuestaPreguntaBPDTO(
           int secuenciaPregunta,
           int secuenciaRespuesta,
           String textoRespuesta) {
        super(
            secuenciaPregunta,
            secuenciaRespuesta);
        this.textoRespuesta = textoRespuesta;
    }


    /**
     * Gets the textoRespuesta value for this OpcionRespuestaPreguntaBPDTO.
     * 
     * @return textoRespuesta
     */
    public String getTextoRespuesta() {
        return textoRespuesta;
    }


    /**
     * Sets the textoRespuesta value for this OpcionRespuestaPreguntaBPDTO.
     * 
     * @param textoRespuesta
     */
    public void setTextoRespuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof OpcionRespuestaPreguntaBPDTO)) return false;
        OpcionRespuestaPreguntaBPDTO other = (OpcionRespuestaPreguntaBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = super.equals(obj) && 
            ((this.textoRespuesta==null && other.getTextoRespuesta()==null) || 
             (this.textoRespuesta!=null &&
              this.textoRespuesta.equals(other.getTextoRespuesta())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = super.hashCode();
        if (getTextoRespuesta() != null) {
            _hashCode += getTextoRespuesta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(OpcionRespuestaPreguntaBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "OpcionRespuestaPreguntaBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("textoRespuesta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "textoRespuesta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
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
