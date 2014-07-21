/**
 * CuestionarioBPDTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.cifin.confrontav2plusws.dto.basicoplus;

public class CuestionarioBPDTO  implements java.io.Serializable {
    private String claveCIFIN;

    private int codigoCuestionario;

    private int codigoTipoCuestionario;

    private DatosPlusBPDTO datosPlus;

    private TerceroBPDTO datosTercero;

    private String descripcionCuestionario;

    private HuellaBPDTO[] huellaConsulta;

    private PreguntaBPDTO[] listadoPreguntas;

    private RespuestaBPDTO respuestaProceso;

    private long secuenciaCuestionario;

    public CuestionarioBPDTO() {
    }

    public CuestionarioBPDTO(
           String claveCIFIN,
           int codigoCuestionario,
           int codigoTipoCuestionario,
           DatosPlusBPDTO datosPlus,
           TerceroBPDTO datosTercero,
           String descripcionCuestionario,
           HuellaBPDTO[] huellaConsulta,
           PreguntaBPDTO[] listadoPreguntas,
           RespuestaBPDTO respuestaProceso,
           long secuenciaCuestionario) {
           this.claveCIFIN = claveCIFIN;
           this.codigoCuestionario = codigoCuestionario;
           this.codigoTipoCuestionario = codigoTipoCuestionario;
           this.datosPlus = datosPlus;
           this.datosTercero = datosTercero;
           this.descripcionCuestionario = descripcionCuestionario;
           this.huellaConsulta = huellaConsulta;
           this.listadoPreguntas = listadoPreguntas;
           this.respuestaProceso = respuestaProceso;
           this.secuenciaCuestionario = secuenciaCuestionario;
    }


    /**
     * Gets the claveCIFIN value for this CuestionarioBPDTO.
     * 
     * @return claveCIFIN
     */
    public String getClaveCIFIN() {
        return claveCIFIN;
    }


    /**
     * Sets the claveCIFIN value for this CuestionarioBPDTO.
     * 
     * @param claveCIFIN
     */
    public void setClaveCIFIN(String claveCIFIN) {
        this.claveCIFIN = claveCIFIN;
    }


    /**
     * Gets the codigoCuestionario value for this CuestionarioBPDTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this CuestionarioBPDTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the codigoTipoCuestionario value for this CuestionarioBPDTO.
     * 
     * @return codigoTipoCuestionario
     */
    public int getCodigoTipoCuestionario() {
        return codigoTipoCuestionario;
    }


    /**
     * Sets the codigoTipoCuestionario value for this CuestionarioBPDTO.
     * 
     * @param codigoTipoCuestionario
     */
    public void setCodigoTipoCuestionario(int codigoTipoCuestionario) {
        this.codigoTipoCuestionario = codigoTipoCuestionario;
    }


    /**
     * Gets the datosPlus value for this CuestionarioBPDTO.
     * 
     * @return datosPlus
     */
    public DatosPlusBPDTO getDatosPlus() {
        return datosPlus;
    }


    /**
     * Sets the datosPlus value for this CuestionarioBPDTO.
     * 
     * @param datosPlus
     */
    public void setDatosPlus(DatosPlusBPDTO datosPlus) {
        this.datosPlus = datosPlus;
    }


    /**
     * Gets the datosTercero value for this CuestionarioBPDTO.
     * 
     * @return datosTercero
     */
    public TerceroBPDTO getDatosTercero() {
        return datosTercero;
    }


    /**
     * Sets the datosTercero value for this CuestionarioBPDTO.
     * 
     * @param datosTercero
     */
    public void setDatosTercero(TerceroBPDTO datosTercero) {
        this.datosTercero = datosTercero;
    }


    /**
     * Gets the descripcionCuestionario value for this CuestionarioBPDTO.
     * 
     * @return descripcionCuestionario
     */
    public String getDescripcionCuestionario() {
        return descripcionCuestionario;
    }


    /**
     * Sets the descripcionCuestionario value for this CuestionarioBPDTO.
     * 
     * @param descripcionCuestionario
     */
    public void setDescripcionCuestionario(String descripcionCuestionario) {
        this.descripcionCuestionario = descripcionCuestionario;
    }


    /**
     * Gets the huellaConsulta value for this CuestionarioBPDTO.
     * 
     * @return huellaConsulta
     */
    public HuellaBPDTO[] getHuellaConsulta() {
        return huellaConsulta;
    }


    /**
     * Sets the huellaConsulta value for this CuestionarioBPDTO.
     * 
     * @param huellaConsulta
     */
    public void setHuellaConsulta(HuellaBPDTO[] huellaConsulta) {
        this.huellaConsulta = huellaConsulta;
    }


    /**
     * Gets the listadoPreguntas value for this CuestionarioBPDTO.
     * 
     * @return listadoPreguntas
     */
    public PreguntaBPDTO[] getListadoPreguntas() {
        return listadoPreguntas;
    }


    /**
     * Sets the listadoPreguntas value for this CuestionarioBPDTO.
     * 
     * @param listadoPreguntas
     */
    public void setListadoPreguntas(PreguntaBPDTO[] listadoPreguntas) {
        this.listadoPreguntas = listadoPreguntas;
    }


    /**
     * Gets the respuestaProceso value for this CuestionarioBPDTO.
     * 
     * @return respuestaProceso
     */
    public RespuestaBPDTO getRespuestaProceso() {
        return respuestaProceso;
    }


    /**
     * Sets the respuestaProceso value for this CuestionarioBPDTO.
     * 
     * @param respuestaProceso
     */
    public void setRespuestaProceso(RespuestaBPDTO respuestaProceso) {
        this.respuestaProceso = respuestaProceso;
    }


    /**
     * Gets the secuenciaCuestionario value for this CuestionarioBPDTO.
     * 
     * @return secuenciaCuestionario
     */
    public long getSecuenciaCuestionario() {
        return secuenciaCuestionario;
    }


    /**
     * Sets the secuenciaCuestionario value for this CuestionarioBPDTO.
     * 
     * @param secuenciaCuestionario
     */
    public void setSecuenciaCuestionario(long secuenciaCuestionario) {
        this.secuenciaCuestionario = secuenciaCuestionario;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof CuestionarioBPDTO)) return false;
        CuestionarioBPDTO other = (CuestionarioBPDTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.claveCIFIN==null && other.getClaveCIFIN()==null) || 
             (this.claveCIFIN!=null &&
              this.claveCIFIN.equals(other.getClaveCIFIN()))) &&
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            this.codigoTipoCuestionario == other.getCodigoTipoCuestionario() &&
            ((this.datosPlus==null && other.getDatosPlus()==null) || 
             (this.datosPlus!=null &&
              this.datosPlus.equals(other.getDatosPlus()))) &&
            ((this.datosTercero==null && other.getDatosTercero()==null) || 
             (this.datosTercero!=null &&
              this.datosTercero.equals(other.getDatosTercero()))) &&
            ((this.descripcionCuestionario==null && other.getDescripcionCuestionario()==null) || 
             (this.descripcionCuestionario!=null &&
              this.descripcionCuestionario.equals(other.getDescripcionCuestionario()))) &&
            ((this.huellaConsulta==null && other.getHuellaConsulta()==null) || 
             (this.huellaConsulta!=null &&
              java.util.Arrays.equals(this.huellaConsulta, other.getHuellaConsulta()))) &&
            ((this.listadoPreguntas==null && other.getListadoPreguntas()==null) || 
             (this.listadoPreguntas!=null &&
              java.util.Arrays.equals(this.listadoPreguntas, other.getListadoPreguntas()))) &&
            ((this.respuestaProceso==null && other.getRespuestaProceso()==null) || 
             (this.respuestaProceso!=null &&
              this.respuestaProceso.equals(other.getRespuestaProceso()))) &&
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
        if (getClaveCIFIN() != null) {
            _hashCode += getClaveCIFIN().hashCode();
        }
        _hashCode += getCodigoCuestionario();
        _hashCode += getCodigoTipoCuestionario();
        if (getDatosPlus() != null) {
            _hashCode += getDatosPlus().hashCode();
        }
        if (getDatosTercero() != null) {
            _hashCode += getDatosTercero().hashCode();
        }
        if (getDescripcionCuestionario() != null) {
            _hashCode += getDescripcionCuestionario().hashCode();
        }
        if (getHuellaConsulta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHuellaConsulta());
                 i++) {
                Object obj = java.lang.reflect.Array.get(getHuellaConsulta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getListadoPreguntas() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getListadoPreguntas());
                 i++) {
                Object obj = java.lang.reflect.Array.get(getListadoPreguntas(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getRespuestaProceso() != null) {
            _hashCode += getRespuestaProceso().hashCode();
        }
        _hashCode += new Long(getSecuenciaCuestionario()).hashCode();
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CuestionarioBPDTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "CuestionarioBPDTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("claveCIFIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "claveCIFIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoTipoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoTipoCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datosPlus");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datosPlus"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "DatosPlusBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datosTercero");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datosTercero"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "TerceroBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descripcionCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descripcionCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("huellaConsulta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "huellaConsulta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "HuellaBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listadoPreguntas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "listadoPreguntas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "PreguntaBPDTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respuestaProceso");
        elemField.setXmlName(new javax.xml.namespace.QName("", "respuestaProceso"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://basicoplus.dto.confrontav2plusws.cifin.asobancaria.com", "RespuestaBPDTO"));
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
