/**
 * CuestionarioULTRADTO.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package co.cifin.confrontaultra.dto.ultra;

public class CuestionarioULTRADTO  implements java.io.Serializable {
    private co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO[] listadoPreguntas;

    private int codigoCuestionario;

    private java.lang.String claveCIFIN;

    private co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso;

    private co.cifin.confrontaultra.dto.ultra.TerceroULTRADTO datosTercero;

    private long secuenciaCuestionario;

    private co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO[] huellaConsulta;

    private java.lang.String descripcionCuestionario;

    public CuestionarioULTRADTO() {
    }

    public CuestionarioULTRADTO(
           co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO[] listadoPreguntas,
           int codigoCuestionario,
           java.lang.String claveCIFIN,
           co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso,
           co.cifin.confrontaultra.dto.ultra.TerceroULTRADTO datosTercero,
           long secuenciaCuestionario,
           co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO[] huellaConsulta,
           java.lang.String descripcionCuestionario) {
           this.listadoPreguntas = listadoPreguntas;
           this.codigoCuestionario = codigoCuestionario;
           this.claveCIFIN = claveCIFIN;
           this.respuestaProceso = respuestaProceso;
           this.datosTercero = datosTercero;
           this.secuenciaCuestionario = secuenciaCuestionario;
           this.huellaConsulta = huellaConsulta;
           this.descripcionCuestionario = descripcionCuestionario;
    }


    /**
     * Gets the listadoPreguntas value for this CuestionarioULTRADTO.
     * 
     * @return listadoPreguntas
     */
    public co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO[] getListadoPreguntas() {
        return listadoPreguntas;
    }


    /**
     * Sets the listadoPreguntas value for this CuestionarioULTRADTO.
     * 
     * @param listadoPreguntas
     */
    public void setListadoPreguntas(co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO[] listadoPreguntas) {
        this.listadoPreguntas = listadoPreguntas;
    }


    /**
     * Gets the codigoCuestionario value for this CuestionarioULTRADTO.
     * 
     * @return codigoCuestionario
     */
    public int getCodigoCuestionario() {
        return codigoCuestionario;
    }


    /**
     * Sets the codigoCuestionario value for this CuestionarioULTRADTO.
     * 
     * @param codigoCuestionario
     */
    public void setCodigoCuestionario(int codigoCuestionario) {
        this.codigoCuestionario = codigoCuestionario;
    }


    /**
     * Gets the claveCIFIN value for this CuestionarioULTRADTO.
     * 
     * @return claveCIFIN
     */
    public java.lang.String getClaveCIFIN() {
        return claveCIFIN;
    }


    /**
     * Sets the claveCIFIN value for this CuestionarioULTRADTO.
     * 
     * @param claveCIFIN
     */
    public void setClaveCIFIN(java.lang.String claveCIFIN) {
        this.claveCIFIN = claveCIFIN;
    }


    /**
     * Gets the respuestaProceso value for this CuestionarioULTRADTO.
     * 
     * @return respuestaProceso
     */
    public co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO getRespuestaProceso() {
        return respuestaProceso;
    }


    /**
     * Sets the respuestaProceso value for this CuestionarioULTRADTO.
     * 
     * @param respuestaProceso
     */
    public void setRespuestaProceso(co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO respuestaProceso) {
        this.respuestaProceso = respuestaProceso;
    }


    /**
     * Gets the datosTercero value for this CuestionarioULTRADTO.
     * 
     * @return datosTercero
     */
    public co.cifin.confrontaultra.dto.ultra.TerceroULTRADTO getDatosTercero() {
        return datosTercero;
    }


    /**
     * Sets the datosTercero value for this CuestionarioULTRADTO.
     * 
     * @param datosTercero
     */
    public void setDatosTercero(co.cifin.confrontaultra.dto.ultra.TerceroULTRADTO datosTercero) {
        this.datosTercero = datosTercero;
    }


    /**
     * Gets the secuenciaCuestionario value for this CuestionarioULTRADTO.
     * 
     * @return secuenciaCuestionario
     */
    public long getSecuenciaCuestionario() {
        return secuenciaCuestionario;
    }


    /**
     * Sets the secuenciaCuestionario value for this CuestionarioULTRADTO.
     * 
     * @param secuenciaCuestionario
     */
    public void setSecuenciaCuestionario(long secuenciaCuestionario) {
        this.secuenciaCuestionario = secuenciaCuestionario;
    }


    /**
     * Gets the huellaConsulta value for this CuestionarioULTRADTO.
     * 
     * @return huellaConsulta
     */
    public co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO[] getHuellaConsulta() {
        return huellaConsulta;
    }


    /**
     * Sets the huellaConsulta value for this CuestionarioULTRADTO.
     * 
     * @param huellaConsulta
     */
    public void setHuellaConsulta(co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO[] huellaConsulta) {
        this.huellaConsulta = huellaConsulta;
    }


    /**
     * Gets the descripcionCuestionario value for this CuestionarioULTRADTO.
     * 
     * @return descripcionCuestionario
     */
    public java.lang.String getDescripcionCuestionario() {
        return descripcionCuestionario;
    }


    /**
     * Sets the descripcionCuestionario value for this CuestionarioULTRADTO.
     * 
     * @param descripcionCuestionario
     */
    public void setDescripcionCuestionario(java.lang.String descripcionCuestionario) {
        this.descripcionCuestionario = descripcionCuestionario;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof CuestionarioULTRADTO)) return false;
        CuestionarioULTRADTO other = (CuestionarioULTRADTO) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.listadoPreguntas==null && other.getListadoPreguntas()==null) || 
             (this.listadoPreguntas!=null &&
              java.util.Arrays.equals(this.listadoPreguntas, other.getListadoPreguntas()))) &&
            this.codigoCuestionario == other.getCodigoCuestionario() &&
            ((this.claveCIFIN==null && other.getClaveCIFIN()==null) || 
             (this.claveCIFIN!=null &&
              this.claveCIFIN.equals(other.getClaveCIFIN()))) &&
            ((this.respuestaProceso==null && other.getRespuestaProceso()==null) || 
             (this.respuestaProceso!=null &&
              this.respuestaProceso.equals(other.getRespuestaProceso()))) &&
            ((this.datosTercero==null && other.getDatosTercero()==null) || 
             (this.datosTercero!=null &&
              this.datosTercero.equals(other.getDatosTercero()))) &&
            this.secuenciaCuestionario == other.getSecuenciaCuestionario() &&
            ((this.huellaConsulta==null && other.getHuellaConsulta()==null) || 
             (this.huellaConsulta!=null &&
              java.util.Arrays.equals(this.huellaConsulta, other.getHuellaConsulta()))) &&
            ((this.descripcionCuestionario==null && other.getDescripcionCuestionario()==null) || 
             (this.descripcionCuestionario!=null &&
              this.descripcionCuestionario.equals(other.getDescripcionCuestionario())));
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
        if (getListadoPreguntas() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getListadoPreguntas());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getListadoPreguntas(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        _hashCode += getCodigoCuestionario();
        if (getClaveCIFIN() != null) {
            _hashCode += getClaveCIFIN().hashCode();
        }
        if (getRespuestaProceso() != null) {
            _hashCode += getRespuestaProceso().hashCode();
        }
        if (getDatosTercero() != null) {
            _hashCode += getDatosTercero().hashCode();
        }
        _hashCode += new Long(getSecuenciaCuestionario()).hashCode();
        if (getHuellaConsulta() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getHuellaConsulta());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getHuellaConsulta(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getDescripcionCuestionario() != null) {
            _hashCode += getDescripcionCuestionario().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(CuestionarioULTRADTO.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "CuestionarioULTRADTO"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("listadoPreguntas");
        elemField.setXmlName(new javax.xml.namespace.QName("", "listadoPreguntas"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "PreguntaULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("codigoCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "codigoCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("claveCIFIN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "claveCIFIN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://schemas.xmlsoap.org/soap/encoding/", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("respuestaProceso");
        elemField.setXmlName(new javax.xml.namespace.QName("", "respuestaProceso"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "RespuestaULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("datosTercero");
        elemField.setXmlName(new javax.xml.namespace.QName("", "datosTercero"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "TerceroULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("secuenciaCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "secuenciaCuestionario"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "long"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("huellaConsulta");
        elemField.setXmlName(new javax.xml.namespace.QName("", "huellaConsulta"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "HuellaULTRADTO"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("descripcionCuestionario");
        elemField.setXmlName(new javax.xml.namespace.QName("", "descripcionCuestionario"));
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
