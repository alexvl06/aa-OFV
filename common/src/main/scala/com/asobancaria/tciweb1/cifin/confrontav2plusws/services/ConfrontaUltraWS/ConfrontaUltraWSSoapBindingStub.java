/**
 * ConfrontaUltraWSSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS;

public class ConfrontaUltraWSSoapBindingStub extends org.apache.axis.client.Stub implements com.asobancaria.tciweb1.cifin.confrontav2plusws.services.ConfrontaUltraWS.ConfrontaUltraWebService {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[3];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("obtenerCuestionario");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosSeguridadULTRADTO"), co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametros"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosULTRADTO"), co.cifin.confrontaultra.dto.ultra.ParametrosULTRADTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "CuestionarioULTRADTO"));
        oper.setReturnClass(co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "obtenerCuestionarioReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("obtenerCuestionarioAdicional");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosSeguridadULTRADTO"), co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametros"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosAdicionalULTRADTO"), co.cifin.confrontaultra.dto.ultra.ParametrosAdicionalULTRADTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "CuestionarioULTRADTO"));
        oper.setReturnClass(co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "obtenerCuestionarioAdicionalReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("evaluarCuestionario");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_parametrosSeguridad"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosSeguridadULTRADTO"), co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO.class, false, false);
        oper.addParameter(param);
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("", "p_respuestaCuestionario"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://ultras.dto.confrontaultra.cifin.co", "RespuestaCuestionarioULTRADTO"), co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ResultadoEvaluacionCuestionarioULTRADTO"));
        oper.setReturnClass(co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO.class);
        oper.setReturnQName(new javax.xml.namespace.QName("", "evaluarCuestionarioReturn"));
        oper.setStyle(org.apache.axis.constants.Style.RPC);
        oper.setUse(org.apache.axis.constants.Use.ENCODED);
        _operations[2] = oper;

    }

    public ConfrontaUltraWSSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public ConfrontaUltraWSSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public ConfrontaUltraWSSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "CuestionarioULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "HuellaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "OpcionRespuestaPreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosAdicionalULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.ParametrosAdicionalULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosSeguridadULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ParametrosULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.ParametrosULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "PreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "RespuestaPreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.RespuestaPreguntaULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "RespuestaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.RespuestaULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "ResultadoEvaluacionCuestionarioULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "TerceroULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.TerceroULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://ultras.dto.confrontaultra.cifin.co", "RespuestaCuestionarioULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ArrayOf_tns1_HuellaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.HuellaULTRADTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "HuellaULTRADTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ArrayOf_tns1_OpcionRespuestaPreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.OpcionRespuestaPreguntaULTRADTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "OpcionRespuestaPreguntaULTRADTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ArrayOf_tns1_PreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.PreguntaULTRADTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "PreguntaULTRADTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("https://tciweb1.asobancaria.com/cifin/confrontav2plusws/services/ConfrontaUltraWS", "ArrayOf_tns1_RespuestaPreguntaULTRADTO");
            cachedSerQNames.add(qName);
            cls = co.cifin.confrontaultra.dto.ultra.RespuestaPreguntaULTRADTO[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://ultra.dto.confrontaultra.cifin.co", "RespuestaPreguntaULTRADTO");
            qName2 = null;
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
                    _call.setEncodingStyle(org.apache.axis.Constants.URI_SOAP11_ENC);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO obtenerCuestionario(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultra.ParametrosULTRADTO p_parametros) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontaultra.cifin.asobancaria.com", "obtenerCuestionario"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {p_parametrosSeguridad, p_parametros});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO) _resp;
            } catch (java.lang.Exception _exception) {
                return (co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO) org.apache.axis.utils.JavaUtils.convert(_resp, co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO obtenerCuestionarioAdicional(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultra.ParametrosAdicionalULTRADTO p_parametros) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontaultra.cifin.asobancaria.com", "obtenerCuestionarioAdicional"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {p_parametrosSeguridad, p_parametros});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO) _resp;
            } catch (java.lang.Exception _exception) {
                return (co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO) org.apache.axis.utils.JavaUtils.convert(_resp, co.cifin.confrontaultra.dto.ultra.CuestionarioULTRADTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO evaluarCuestionario(co.cifin.confrontaultra.dto.ultra.ParametrosSeguridadULTRADTO p_parametrosSeguridad, co.cifin.confrontaultra.dto.ultras.RespuestaCuestionarioULTRADTO p_respuestaCuestionario) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("http://ws.confrontaultra.cifin.asobancaria.com", "evaluarCuestionario"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {p_parametrosSeguridad, p_respuestaCuestionario});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO) _resp;
            } catch (java.lang.Exception _exception) {
                return (co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO) org.apache.axis.utils.JavaUtils.convert(_resp, co.cifin.confrontaultra.dto.ultra.ResultadoEvaluacionCuestionarioULTRADTO.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
