package com.luxsoft.impapx.contabilidad


import groovy.transform.ToString
import groovy.transform.EqualsAndHashCode
import com.luxsoft.lx.utils.MonedaUtils
import com.luxsoft.lx.sat.BancoSat

@ToString(excludes='dateCreated,lastUpdated,polizaDet,id,version',includeNames=true,includePackage=false)
@EqualsAndHashCode
class ComprobanteExtranjero {
	
	String numFacExt
	
	String taxId
	
	BigDecimal montoTotal = 0.0
	
	Currency moneda = MonedaUtils.PESOS
	
	BigDecimal tipCamb = 1.0

	Date dateCreated

	Date lastUpdated

    static constraints = {
    	taxId nullable:true
        tipCamb scale:5
    }

    static belongsTo = [polizaDet:PolizaDet]

    String info(){
    	def comp =this
    	def writer = new StringWriter()
    	def builder = new groovy.xml.MarkupBuilder(writer)
    	builder.div{
    	    p('NumFacExt: '+comp.numFacExt)
    	    p('TaxId: ' +comp.taxId)
    	    p('Moneda: '+comp.moneda)
    	    p('TipCamb: '+comp.tipCamb)
    	    p('MontoTotal: '+comp.montoTotal)
    	}
    	return writer.toString()
    }
}


