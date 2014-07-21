/**
 * ParametrosBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class ParametrosBPDTO  implements java.io.Serializable {
    private int codigoCiudad;

    private int codigoCuestionario;

    private int codigoDepartamento;

    private String codigoTipoIdentificacion;

    private String numeroIdentificacion;

    private String telefono;

    public ParametrosBPDTO() {
    }

    public ParametrosBPDTO(
           int codigoCiudad,
           int codigoCuestionario,
           int codigoDepartamento,
           String codigoTipoIdentificacion,
           String numeroIdentificacion,
           String telefono) {
           this.codigoCiudad = codigoCiudad;
           this.codigoCuestionario = codigoCuestionario;
           this.codigoDepartamento = codigoDepartamento;
           this.codigoTipoIdentificacion = codigoTipoIdentificacion;
           this.numeroIdentificacion = numeroIdentificacion;
           this.telefono = telefono;
    }


    /**
     * Gets the codigoCiudad value for this ParametrosBPDTO.
     * 
     * @return codigoCiudad
     */
    public int getCodigoCiudad() {
        return codigoCiudad;
    }


    /**
     * Sets the codigoCiudad value for this ParametrosBPDTO.
     * 
     * @param codigoCiudad
     */
    public void setCodigoCiudad(int codigoCiudad) {
        this.codigoCiudad = codigoCiudad;
    }


    /**
     * Gets the codigoCuestionario value for this ParametrosBPDTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this ParametrosBPDTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the codigoDepartamento value for this ParametrosBPDTO.
     * 
     * @return codigoDepartamento
     */
    public int getCodigoDepartamento() {
        return codigoDepartamento;
    }


    /**
     * Sets the codigoDepartamento value for this ParametrosBPDTO.
     * 
     * @param codigoDepartamento
     */
    public void setCodigoDepartamento(int codigoDepartamento) {
        this.codigoDepartamento = codigoDepartamento;
    }


    /**
     * Gets the codigoTipoIdentificacion value for this ParametrosBPDTO.
     * 
     * @return codigoTipoIdentificacion
     */
    public String getCodigoTipoIdentificacion() {
        return codigoTipoIdentificacion;
    }


    /**
     * Sets the codigoTipoIdentificacion value for this ParametrosBPDTO.
     * 
     * @param codigoTipoIdentificacion
     */
    public void setCodigoTipoIdentificacion(String codigoTipoIdentificacion) {
        this.codigoTipoIdentificacion = codigoTipoIdentificacion;
    }


    /**
     * Gets the numeroIdentificacion value for this ParametrosBPDTO.
     * 
     * @return numeroIdentificacion
     */
    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }


    /**
     * Sets the numeroIdentificacion value for this ParametrosBPDTO.
     * 
     * @param numeroIdentificacion
     */
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }


    /**
     * Gets the telefono value for this ParametrosBPDTO.
     * 
     * @return telefono
     */
    public String getTelefono() {
        return telefono;
    }


    /**
     * Sets the telefono value for this ParametrosBPDTO.
     * 
     * @param telefono
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof ParametrosBPDTO)) return false;
        ParametrosBPDTO other = (ParametrosBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.codigoCiudad == other.getCodigoCiudad() &&
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            this.codigoDepartamento == other.getCodigoDepartamento() &&
            ((this.codigoTipoIdentificacion==null && other.getCodigoTipoIdentificacion()==null) || 
             (this.codigoTipoIdentificacion!=null &&
              this.codigoTipoIdentificacion.equals(other.getCodigoTipoIdentificacion()))) &&
            ((this.numeroIdentificacion==null && other.getNumeroIdentificacion()==null) || 
             (this.numeroIdentificacion!=null &&
              this.numeroIdentificacion.equals(other.getNumeroIdentificacion()))) &&
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
        _hashCode += getCodigoCuestionario();
        _hashCode += getCodigoDepartamento();
        if (getCodigoTipoIdentificacion() != null) {
            _hashCode += getCodigoTipoIdentificacion().hashCode();
        }
        if (getNumeroIdentificacion() != null) {
            _hashCode += getNumeroIdentificacion().hashCode();
        }
        if (getTelefono() != null) {
            _hashCode += getTelefono().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(ParametrosBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "ParametrosBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCiudad");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCiudad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCuestionario"));
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
        elemField.setFieldName("codigoTipoIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoTipoIdentificacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numeroIdentificacion");
        elemField.setXmlName(new javax.xml.namespace.QName("", "numeroIdentificacion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
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
