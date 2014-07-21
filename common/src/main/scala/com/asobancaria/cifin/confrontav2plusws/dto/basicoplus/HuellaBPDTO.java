/**
 * HuellaBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class HuellaBPDTO  implements java.io.Serializable {
    private int cantidadConsultas;

    private String fechaConsulta;

    private String nombreEntidad;

    private String resultadoConsulta;

    public HuellaBPDTO() {
    }

    public HuellaBPDTO(
           int cantidadConsultas,
           String fechaConsulta,
           String nombreEntidad,
           String resultadoConsulta) {
           this.cantidadConsultas = cantidadConsultas;
           this.fechaConsulta = fechaConsulta;
           this.nombreEntidad = nombreEntidad;
           this.resultadoConsulta = resultadoConsulta;
    }


    /**
     * Gets the cantidadConsultas value for this HuellaBPDTO.
     * 
     * @return cantidadConsultas
     */
    public int getCantidadConsultas() {
        return cantidadConsultas;
    }


    /**
     * Sets the cantidadConsultas value for this HuellaBPDTO.
     * 
     * @param cantidadConsultas
     */
    public void setCantidadConsultas(int cantidadConsultas) {
        this.cantidadConsultas = cantidadConsultas;
    }


    /**
     * Gets the fechaConsulta value for this HuellaBPDTO.
     * 
     * @return fechaConsulta
     */
    public String getFechaConsulta() {
        return fechaConsulta;
    }


    /**
     * Sets the fechaConsulta value for this HuellaBPDTO.
     * 
     * @param fechaConsulta
     */
    public void setFechaConsulta(String fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }


    /**
     * Gets the nombreEntidad value for this HuellaBPDTO.
     * 
     * @return nombreEntidad
     */
    public String getNombreEntidad() {
        return nombreEntidad;
    }


    /**
     * Sets the nombreEntidad value for this HuellaBPDTO.
     * 
     * @param nombreEntidad
     */
    public void setNombreEntidad(String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }


    /**
     * Gets the resultadoConsulta value for this HuellaBPDTO.
     * 
     * @return resultadoConsulta
     */
    public String getResultadoConsulta() {
        return resultadoConsulta;
    }


    /**
     * Sets the resultadoConsulta value for this HuellaBPDTO.
     * 
     * @param resultadoConsulta
     */
    public void setResultadoConsulta(String resultadoConsulta) {
        this.resultadoConsulta = resultadoConsulta;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof HuellaBPDTO)) return false;
        HuellaBPDTO other = (HuellaBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.cantidadConsultas == other.getCantidadConsultas() &&
            ((this.fechaConsulta==null && other.getFechaConsulta()==null) || 
             (this.fechaConsulta!=null &&
              this.fechaConsulta.equals(other.getFechaConsulta()))) &&
            ((this.nombreEntidad==null && other.getNombreEntidad()==null) || 
             (this.nombreEntidad!=null &&
              this.nombreEntidad.equals(other.getNombreEntidad()))) &&
            ((this.resultadoConsulta==null && other.getResultadoConsulta()==null) || 
             (this.resultadoConsulta!=null &&
              this.resultadoConsulta.equals(other.getResultadoConsulta())));
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
        _hashCode += getCantidadConsultas();
        if (getFechaConsulta() != null) {
            _hashCode += getFechaConsulta().hashCode();
        }
        if (getNombreEntidad() != null) {
            _hashCode += getNombreEntidad().hashCode();
        }
        if (getResultadoConsulta() != null) {
            _hashCode += getResultadoConsulta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HuellaBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "HuellaBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("cantidadConsultas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "cantidadConsultas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fechaConsulta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "fechaConsulta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nombreEntidad");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nombreEntidad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultadoConsulta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultadoConsulta"));
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
