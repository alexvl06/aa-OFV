/**
 * RespuestaCuestionarioBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class RespuestaCuestionarioBPDTO  implements java.io.Serializable {
    private int codigoCuestionario;

    private RespuestaPreguntaBPDTO[] respuestas;

    private long secuenciaCuestionario;

    public RespuestaCuestionarioBPDTO() {
    }

    public RespuestaCuestionarioBPDTO(
           int codigoCuestionario,
           RespuestaPreguntaBPDTO[] respuestas,
           long secuenciaCuestionario) {
           this.codigoCuestionario = codigoCuestionario;
           this.respuestas = respuestas;
           this.secuenciaCuestionario = secuenciaCuestionario;
    }


    /**
     * Gets the codigoCuestionario value for this RespuestaCuestionarioBPDTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this RespuestaCuestionarioBPDTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the respuestas value for this RespuestaCuestionarioBPDTO.
     * 
     * @return respuestas
     */
    public RespuestaPreguntaBPDTO[] getRespuestas() {
        return respuestas;
    }


    /**
     * Sets the respuestas value for this RespuestaCuestionarioBPDTO.
     * 
     * @param respuestas
     */
    public void setRespuestas(RespuestaPreguntaBPDTO[] respuestas) {
        this.respuestas = respuestas;
    }


    /**
     * Gets the secuenciaCuestionario value for this RespuestaCuestionarioBPDTO.
     * 
     * @return secuenciaCuestionario
     */
    public long getSecuenciaCuestionario() {
        return secuenciaCuestionario;
    }


    /**
     * Sets the secuenciaCuestionario value for this RespuestaCuestionarioBPDTO.
     * 
     * @param secuenciaCuestionario
     */
    public void setSecuenciaCuestionario(long secuenciaCuestionario) {
        this.secuenciaCuestionario = secuenciaCuestionario;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof RespuestaCuestionarioBPDTO)) return false;
        RespuestaCuestionarioBPDTO other = (RespuestaCuestionarioBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            ((this.respuestas==null && other.getRespuestas()==null) || 
             (this.respuestas!=null &&
              java.util.Arrays.equals(this.respuestas, other.getRespuestas()))) &&
            this.secuenciaCuestionario == other.getSecuenciaCuestionario();
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
        _hashCode += getCodigoCuestionario();
        if (getRespuestas() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getRespuestas());
                 i++) {
                Object obj = java.lang.reflect.Array.get(getRespuestas(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += new Long(getSecuenciaCuestionario()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RespuestaCuestionarioBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaCuestionarioBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respuestas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "respuestas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaPreguntaBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
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
