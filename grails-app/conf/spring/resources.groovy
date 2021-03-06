import grails.util.Environment
import org.springframework.web.servlet.i18n.FixedLocaleResolver

import com.luxsoft.cfdi.CfdiCadenaBuilder
import com.luxsoft.cfdi.CfdiSellador
import com.luxsoft.cfdi.CfdiTimbrador
import com.luxsoft.lx.MarshallerRegistrar
import com.luxsoft.cfdi.retenciones.*
import com.luxsoft.nomina.*

// Place your Spring DSL code here
beans = {
	
	
	switch(Environment.current){
		
		case Environment.PRODUCTION:
			cfdiTimbrador(CfdiTimbrador){
				timbradoDePrueba=false
			}
			break
		case Environment.DEVELOPMENT:
			cfdiTimbrador(CfdiTimbrador){
			timbradoDePrueba=true
			}
			break
		case Environment.TEST:
			cfdiTimbrador(CfdiTimbrador){
				timbradoDePrueba=true
			}
			break
		case Environment.CUSTOM:
			cfdiTimbrador(CfdiTimbrador){
				timbradoDePrueba=false
			}
			break
		
		
	}
	
	customPropertyEditorRegistar(util.CustomPropertyEditorRegistar){
		dateEditor={util.CustomDateEditor e->
			formats=['dd/MM/yyyy','dd/MM/yy']
			allowEmpty=true
		}
	}
	/*
	localeResolver(org.springframework.web.servlet.i18n.SessionLocaleResolver){
		defaultLocale=new Locale("es","mx")
		java.util.Locale.setDefault(defaultLocale)
	}*/
	localeResolver(FixedLocaleResolver,Locale.US){
		defaultLocale=new Locale("es","mx")
		Locale.setDefault(defaultLocale)
	}
	
	
	
	cfdiCadenaBuilder(CfdiCadenaBuilder){
		//xsltFileName="web-app/sat/cadenaoriginal_3_2.xslt"
	}
	
	cfdiSellador(CfdiSellador){
		cadenaBuilder=ref("cfdiCadenaBuilder")
	}

	lxRegistar(MarshallerRegistrar){}

	retencionesBuilder(RetencionesBuilder){
		cadenaBuilder=ref('retencionesCadenaBuilder')
		retencionSellador=ref('retencionesSellador')
	}

	retencionesCadenaBuilder(RetencionesCadenaBuilder){}

	retencionesSellador(RetencionesSellador){}

	switch(Environment.current){
		
		case Environment.PRODUCTION:
			retencionesTimbrador(RetencionesTimbrador){
				timbradoDePrueba=false
			}

			break
		case Environment.DEVELOPMENT:
			retencionesTimbrador(RetencionesTimbrador){
				timbradoDePrueba=true
			}
			break
		case Environment.TEST:
			retencionesTimbrador(RetencionesTimbrador){
			timbradoDePrueba=true
		}
		
	}
	// Cfdi builder para la generacion de recibos de honorarios en pagos de asimilados a salarios y otros
	nominaCfdiBuilder(NominaCfdiBuilder){}
	
	
}
