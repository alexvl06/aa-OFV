/**
 * HuellaULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class HuellaULTRADTO  implements java.io.Serializable {
    private java.lang.String nombreEntidad;

    private int cantidadConsultas;

    private java.lang.String fechaConsulta;

    private java.lang.String resultadoConsulta;

    public HuellaULTRADTO() {
    }

    public HuellaULTRADTO(
           java.lang.String nombreEntidad,
           int cantidadConsultas,
           java.lang.String fechaConsulta,
           java.lang.String resultadoConsulta) {
           this.nombreEntidad = nombreEntidad;
           this.cantidadConsultas = cantidadConsultas;
           this.fechaConsulta = fechaConsulta;
           this.resultadoConsulta = resultadoConsulta;
    }


    /**
     * Gets the nombreEntidad value for this HuellaULTRADTO.
     * 
     * @return nombreEntidad
     */
    public java.lang.String getNombreEntidad() {
        return nombreEntidad;
    }


    /**
     * Sets the nombreEntidad value for this HuellaULTRADTO.
     * 
     * @param nombreEntidad
     */
    public void setNombreEntidad(java.lang.String nombreEntidad) {
        this.nombreEntidad = nombreEntidad;
    }


    /**
     * Gets the cantidadConsultas value for this HuellaULTRADTO.
     * 
     * @return cantidadConsultas
     */
    public int getCantidadConsultas() {
        return cantidadConsultas;
    }


    /**
     * Sets the cantidadConsultas value for this HuellaULTRADTO.
     * 
     * @param cantidadConsultas
     */
    public void setCantidadConsultas(int cantidadConsultas) {
        this.cantidadConsultas = cantidadConsultas;
    }


    /**
     * Gets the fechaConsulta value for this HuellaULTRADTO.
     * 
     * @return fechaConsulta
     */
    public java.lang.String getFechaConsulta() {
        return fechaConsulta;
    }


    /**
     * Sets the fechaConsulta value for this HuellaULTRADTO.
     * 
     * @param fechaConsulta
     */
    public void setFechaConsulta(java.lang.String fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }


    /**
     * Gets the resultadoConsulta value for this HuellaULTRADTO.
     * 
     * @return resultadoConsulta
     */
    public java.lang.String getResultadoConsulta() {
        return resultadoConsulta;
    }


    /**
     * Sets the resultadoConsulta value for this HuellaULTRADTO.
     * 
     * @param resultadoConsulta
     */
    public void setResultadoConsulta(java.lang.String resultadoConsulta) {
        this.resultadoConsulta = resultadoConsulta;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof HuellaULTRADTO)) return false;
        HuellaULTRADTO other = (HuellaULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.nombreEntidad==null && other.getNombreEntidad()==null) || 
             (this.nombreEntidad!=null &&
              this.nombreEntidad.equals(other.getNombreEntidad()))) &&
            this.cantidadConsultas == other.getCantidadConsultas() &&
            ((this.fechaConsulta==null && other.getFechaConsulta()==null) || 
             (this.fechaConsulta!=null &&
              this.fechaConsulta.equals(other.getFechaConsulta()))) &&
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
        if (getNombreEntidad() != null) {
            _hashCode += getNombreEntidad().hashCode();
        }
        _hashCode += getCantidadConsultas();
        if (getFechaConsulta() != null) {
            _hashCode += getFechaConsulta().hashCode();
        }
        if (getResultadoConsulta() != null) {
            _hashCode += getResultadoConsulta().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(HuellaULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "HuellaULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nombreEntidad");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nombreEntidad"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
