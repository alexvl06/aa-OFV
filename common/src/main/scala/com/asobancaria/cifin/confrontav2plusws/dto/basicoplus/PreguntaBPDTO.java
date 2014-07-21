/**
 * PreguntaBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class PreguntaBPDTO  implements java.io.Serializable {
    private com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO[] listadoRespuestas;

    private int posicionPregunta;

    private int secuenciaPregunta;

    private String textoPregunta;

    public PreguntaBPDTO() {
    }

    public PreguntaBPDTO(
           com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO[] listadoRespuestas,
           int posicionPregunta,
           int secuenciaPregunta,
           String textoPregunta) {
           this.listadoRespuestas = listadoRespuestas;
           this.posicionPregunta = posicionPregunta;
           this.secuenciaPregunta = secuenciaPregunta;
           this.textoPregunta = textoPregunta;
    }


    /**
     * Gets the listadoRespuestas value for this PreguntaBPDTO.
     * 
     * @return listadoRespuestas
     */
    public com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO[] getListadoRespuestas() {
        return listadoRespuestas;
    }


    /**
     * Sets the listadoRespuestas value for this PreguntaBPDTO.
     * 
     * @param listadoRespuestas
     */
    public void setListadoRespuestas(com.asobancaria.cifin.confrontav2plusws.dto.basicoplus.OpcionRespuestaPreguntaBPDTO[] listadoRespuestas) {
        this.listadoRespuestas = listadoRespuestas;
    }


    /**
     * Gets the posicionPregunta value for this PreguntaBPDTO.
     * 
     * @return posicionPregunta
     */
    public int getPosicionPregunta() {
        return posicionPregunta;
    }


    /**
     * Sets the posicionPregunta value for this PreguntaBPDTO.
     * 
     * @param posicionPregunta
     */
    public void setPosicionPregunta(int posicionPregunta) {
        this.posicionPregunta = posicionPregunta;
    }


    /**
     * Gets the secuenciaPregunta value for this PreguntaBPDTO.
     * 
     * @return secuenciaPregunta
     */
    public int getSecuenciaPregunta() {
        return secuenciaPregunta;
    }


    /**
     * Sets the secuenciaPregunta value for this PreguntaBPDTO.
     * 
     * @param secuenciaPregunta
     */
    public void setSecuenciaPregunta(int secuenciaPregunta) {
        this.secuenciaPregunta = secuenciaPregunta;
    }


    /**
     * Gets the textoPregunta value for this PreguntaBPDTO.
     * 
     * @return textoPregunta
     */
    public String getTextoPregunta() {
        return textoPregunta;
    }


    /**
     * Sets the textoPregunta value for this PreguntaBPDTO.
     * 
     * @param textoPregunta
     */
    public void setTextoPregunta(String textoPregunta) {
        this.textoPregunta = textoPregunta;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof PreguntaBPDTO)) return false;
        PreguntaBPDTO other = (PreguntaBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.listadoRespuestas==null && other.getListadoRespuestas()==null) || 
             (this.listadoRespuestas!=null &&
              java.util.Arrays.equals(this.listadoRespuestas, other.getListadoRespuestas()))) &&
            this.posicionPregunta == other.getPosicionPregunta() &&
            this.secuenciaPregunta == other.getSecuenciaPregunta() &&
            ((this.textoPregunta==null && other.getTextoPregunta()==null) || 
             (this.textoPregunta!=null &&
              this.textoPregunta.equals(other.getTextoPregunta())));
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
        if (getListadoRespuestas() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getListadoRespuestas());
                 i++) {
                Object obj = java.lang.reflect.Array.get(getListadoRespuestas(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getPosicionPregunta();
        _hashCode += getSecuenciaPregunta();
        if (getTextoPregunta() != null) {
            _hashCode += getTextoPregunta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PreguntaBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "PreguntaBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listadoRespuestas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "listadoRespuestas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "OpcionRespuestaPreguntaBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("posicionPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "posicionPregunta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaPregunta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("textoPregunta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "textoPregunta"));
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
