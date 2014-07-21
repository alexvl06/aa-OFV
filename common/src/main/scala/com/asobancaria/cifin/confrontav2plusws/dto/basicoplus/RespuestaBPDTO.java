/**
 * RespuestaBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class RespuestaBPDTO  implements java.io.Serializable {
    private int codigoRespuesta;

    private String descripcionRespuesta;

    public RespuestaBPDTO() {
    }

    public RespuestaBPDTO(
           int codigoRespuesta,
           String descripcionRespuesta) {
           this.codigoRespuesta = codigoRespuesta;
           this.descripcionRespuesta = descripcionRespuesta;
    }


    /**
     * Gets the codigoRespuesta value for this RespuestaBPDTO.
     * 
     * @return codigoRespuesta
     */
    public int getCodigoRespuesta() {
        return codigoRespuesta;
    }


    /**
     * Sets the codigoRespuesta value for this RespuestaBPDTO.
     * 
     * @param codigoRespuesta
     */
    public void setCodigoRespuesta(int codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }


    /**
     * Gets the descripcionRespuesta value for this RespuestaBPDTO.
     * 
     * @return descripcionRespuesta
     */
    public String getDescripcionRespuesta() {
        return descripcionRespuesta;
    }


    /**
     * Sets the descripcionRespuesta value for this RespuestaBPDTO.
     * 
     * @param descripcionRespuesta
     */
    public void setDescripcionRespuesta(String descripcionRespuesta) {
        this.descripcionRespuesta = descripcionRespuesta;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof RespuestaBPDTO)) return false;
        RespuestaBPDTO other = (RespuestaBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.codigoRespuesta == other.getCodigoRespuesta() &&
            ((this.descripcionRespuesta==null && other.getDescripcionRespuesta()==null) || 
             (this.descripcionRespuesta!=null &&
              this.descripcionRespuesta.equals(other.getDescripcionRespuesta())));
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
        _hashCode += getCodigoRespuesta();
        if (getDescripcionRespuesta() != null) {
            _hashCode += getDescripcionRespuesta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RespuestaBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoRespuesta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoRespuesta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descripcionRespuesta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descripcionRespuesta"));
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
