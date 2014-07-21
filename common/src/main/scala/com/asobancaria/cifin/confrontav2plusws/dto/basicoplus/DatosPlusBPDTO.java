/**
 * DatosPlusBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class DatosPlusBPDTO  implements java.io.Serializable {
    private int codigoCiudad;

    private int codigoDepartamento;

    private String direccion;

    private int resultadoValidacion;

    private String telefono;

    public DatosPlusBPDTO() {
    }

    public DatosPlusBPDTO(
           int codigoCiudad,
           int codigoDepartamento,
           String direccion,
           int resultadoValidacion,
           String telefono) {
           this.codigoCiudad = codigoCiudad;
           this.codigoDepartamento = codigoDepartamento;
           this.direccion = direccion;
           this.resultadoValidacion = resultadoValidacion;
           this.telefono = telefono;
    }


    /**
     * Gets the codigoCiudad value for this DatosPlusBPDTO.
     * 
     * @return codigoCiudad
     */
    public int getCodigoCiudad() {
        return codigoCiudad;
    }


    /**
     * Sets the codigoCiudad value for this DatosPlusBPDTO.
     * 
     * @param codigoCiudad
     */
    public void setCodigoCiudad(int codigoCiudad) {
        this.codigoCiudad = codigoCiudad;
    }


    /**
     * Gets the codigoDepartamento value for this DatosPlusBPDTO.
     * 
     * @return codigoDepartamento
     */
    public int getCodigoDepartamento() {
        return codigoDepartamento;
    }


    /**
     * Sets the codigoDepartamento value for this DatosPlusBPDTO.
     * 
     * @param codigoDepartamento
     */
    public void setCodigoDepartamento(int codigoDepartamento) {
        this.codigoDepartamento = codigoDepartamento;
    }


    /**
     * Gets the direccion value for this DatosPlusBPDTO.
     * 
     * @return direccion
     */
    public String getDireccion() {
        return direccion;
    }


    /**
     * Sets the direccion value for this DatosPlusBPDTO.
     * 
     * @param direccion
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }


    /**
     * Gets the resultadoValidacion value for this DatosPlusBPDTO.
     * 
     * @return resultadoValidacion
     */
    public int getResultadoValidacion() {
        return resultadoValidacion;
    }


    /**
     * Sets the resultadoValidacion value for this DatosPlusBPDTO.
     * 
     * @param resultadoValidacion
     */
    public void setResultadoValidacion(int resultadoValidacion) {
        this.resultadoValidacion = resultadoValidacion;
    }


    /**
     * Gets the telefono value for this DatosPlusBPDTO.
     * 
     * @return telefono
     */
    public String getTelefono() {
        return telefono;
    }


    /**
     * Sets the telefono value for this DatosPlusBPDTO.
     * 
     * @param telefono
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof DatosPlusBPDTO)) return false;
        DatosPlusBPDTO other = (DatosPlusBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.codigoCiudad == other.getCodigoCiudad() &&
            this.codigoDepartamento == other.getCodigoDepartamento() &&
            ((this.direccion==null && other.getDireccion()==null) || 
             (this.direccion!=null &&
              this.direccion.equals(other.getDireccion()))) &&
            this.resultadoValidacion == other.getResultadoValidacion() &&
            ((this.telefono==null && other.getTelefono()==null) || 
             (this.telefono!=null &&
              this.telefono.equals(other.getTelefono())));
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
        _hashCode += getCodigoCiudad();
        _hashCode += getCodigoDepartamento();
        if (getDireccion() != null) {
            _hashCode += getDireccion().hashCode();
        }
        _hashCode += getResultadoValidacion();
        if (getTelefono() != null) {
            _hashCode += getTelefono().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DatosPlusBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "DatosPlusBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCiudad");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCiudad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoDepartamento");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoDepartamento"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("direccion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "direccion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultadoValidacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultadoValidacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("telefono");
        elemField.setXmlName(new javax.xml.namespace.QName("", "telefono"));
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
