
package com.aafp.afiliado.action;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.aafp.afiliado.dao.UsuarioAfiliadoDAO;
import com.aafp.afiliado.dao.UtilDAO;
import com.aafp.afiliado.dto.AfiliadoDTO;
import com.aafp.afiliado.dto.UsuarioAfiliado;
import com.aafp.afiliado.form.AutoregistroForm;
import com.aafp.afiliado.service.AutoregistroService;
import com.aafp.afiliado.util.constantes.PcrConstantes;


public class AutoregistroAction extends DispatchAction{

	
	
	
	public ActionForward redireccionarAutoregistro(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {
        listarElementosBasicos(request);
	    return mapping.findForward("paginaAutoRegistro");
	}
	
	public ActionForward obtenerDatosAfiliado(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {
		 String pagina = "paginaAutoRegistro";
		 String mensaje = "";
		   Integer tipoResultado = PcrConstantes.NO_EXISTE_ERROR;
	   try {

	   boolean existeClave = false;
	   AutoregistroForm autoregistro = (AutoregistroForm)form;
	   AfiliadoDTO  afiliado = new AfiliadoDTO();
	   afiliado = AutoregistroService.obtenerDatosAfiliado(autoregistro.getTipoDocumento(),autoregistro.getNumeroDocumento());
	  
	   //validar si usuario existe 
	   if(afiliado.getCuspp() == null  || afiliado.getCuspp().trim().equals("T")){
	   	tipoResultado= PcrConstantes.SI_EXISTE_ERROR;
	    mensaje= PcrConstantes.propiedades.getProperty("autoregistro.noExisteCuspp");
	   }else{
	   		
	    // obteniendo afp del usuario 
		   Date ahora = new Date();
	       SimpleDateFormat formateador = new SimpleDateFormat("yyyyMM");
	       String devengue =  formateador.format(ahora);
	       System.out.println("devengue  actual " + devengue);
	     
	       UsuarioAfiliado usuarioCussp = new UsuarioAfiliado();
	       usuarioCussp.setStrCuspp(afiliado.getCuspp());
	       
	       UsuarioAfiliado usuarioAfiliadoDTO = new  UsuarioAfiliado();
	       usuarioAfiliadoDTO.setStrCuspp(afiliado.getCuspp());
	       
	       UsuarioAfiliadoDAO.obtenerDatosAfiliado(usuarioAfiliadoDTO,devengue);
       	   System.out.println(" el AFP actual es ::: " + usuarioAfiliadoDTO.getStrCodAFP());
	      
	       // si no se pudo determinar la afp aparecera vacio
	       if(usuarioAfiliadoDTO.getStrCodAFP() == null || usuarioAfiliadoDTO.getStrCodAFP().equals("00") || usuarioAfiliadoDTO.getStrCodAFP().equals("--")){
	       	afiliado.setNombreAFP("");
	       }else{
	        String descricionAFP = UtilDAO.obtenerDecripcionAFP(usuarioAfiliadoDTO.getStrCodAFP());
	 	    afiliado.setNombreAFP(descricionAFP);
	        	
	       }
	    
		 //validar si tiene clave
	   	existeClave = AutoregistroService.existeClave(afiliado.getCuspp());
	   	if(existeClave){
	     	tipoResultado= PcrConstantes.SI_EXISTE_CUENTA;
		    mensaje= PcrConstantes.propiedades.getProperty("autoregistro.existeClave");	
	   	}else{
	    autoregistro.setApellidoPaterno(afiliado.getApellidoPaterno());
	    autoregistro.setApellidoMaterno(afiliado.getApellidoMaterno());
	    autoregistro.setPrimerNombre(afiliado.getNombre1());
	    autoregistro.setSegundoNombre(afiliado.getNombre2());
	    autoregistro.setFechaNacimiento(afiliado.getFechaNacimiento());
	    autoregistro.setAfp(afiliado.getNombreAFP());
	    request.setAttribute("cuspp",afiliado.getCuspp());
	   	}
	   }   
	   } catch (Exception e) {
	   	System.out.println("El error es ::::  " + e.getLocalizedMessage());
	   	pagina="global.PaginaErrorAutoregistro";
	   }
	   
	   request.setAttribute("mensaje",mensaje);
	   request.setAttribute("tipoResultado",tipoResultado);
	   listarElementosBasicos(request);
	   return mapping.findForward(pagina);
	
	}
   
    public void listarElementosBasicos(HttpServletRequest request) throws Exception{
    ArrayList listaTipoDocumento = new ArrayList();
    ArrayList listDiscado = null;
    listaTipoDocumento = AutoregistroService.listarTipoDocumento(true);
    listDiscado = UtilDAO.listarCodigosDiscado();
	request.setAttribute("listCodDiscado",listDiscado);
    request.setAttribute("listaTipoDocumentos",listaTipoDocumento);
   	}
    
    
    public ActionForward registrarAfiliado(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
    	String mensaje = "";
    	boolean existeExitoRegistro = true;
    	boolean existeClave = false;
 	    Integer tipoResultado = PcrConstantes.TRANSACION_EXITOSA; 
 	    AutoregistroForm autoregistro = (AutoregistroForm)form;
    	String cuspp  = (String)request.getParameter("cuspp");
    	
    	System.out.println("Los telefonos son " +autoregistro.getCodTeleFonoFijo()+"-"+autoregistro.getTelefonoFijo()+" movil es " + autoregistro.getTelefonoMovil());
    	try {
    		existeClave = AutoregistroService.existeClave(cuspp);
    		if(existeClave){
    	     	 tipoResultado= PcrConstantes.SI_EXISTE_CUENTA;
    		     mensaje= PcrConstantes.propiedades.getProperty("autoregistro.existeClave");	
    		     listarElementosBasicos(request);
    	   	}else{
    	   		existeExitoRegistro = AutoregistroService.registrarDatosAfiliado(cuspp,autoregistro);
    	   		if(existeExitoRegistro){
    		     mensaje = PcrConstantes.propiedades.getProperty("autoregistro.registroExitoso");
    	  	     listarElementosBasicos(request);
    	   		}else {
    	   		  mensaje = PcrConstantes.propiedades.getProperty("autoregistro.errorOperacion");
    	  	     listarElementosBasicos(request);
    	   		}
    	   	}
		} catch (Exception e) {
	       mensaje = PcrConstantes.propiedades.getProperty("autoregistro.errorOperacion");
	       tipoResultado = PcrConstantes.ERROR_TRANSACCION;
		}
		
	   	request.setAttribute("mensaje",mensaje);
    	request.setAttribute("tipoResultado",tipoResultado);
		return mapping.findForward("paginaAutoRegistro");
    	
    }
    
    public ActionForward redireccionarInicioWeb(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
	throws Exception {
        
	    return mapping.findForward("inicioWeb");
	}
}
