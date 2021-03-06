<%@ page import="com.luxsoft.impapx.cxc.CXCNota" %>
<%@ page import="com.luxsoft.cfdix.v32.V32CfdiUtils" %>
<!doctype html>
<html>
<head>
	<meta name="layout" content="createForm">
	<g:set var="entityName" value="${message(code: 'CXCNota.label', default: 'CXCNota')}" scope="request"/>
	<g:set var="entity" value="${CXCNotaInstance}" scope="request" />
	<title>Alta de nota</title>
</head>
<body>
	

<content tag="formFields">
	<f:with bean="${entity}" >
		<f:field property="cliente" wrapper="bootstrap3"/>
		<f:field property="fecha" wrapper="bootstrap3"/>
		<f:field property="tipo"  wrapper="bootstrap3" widget-class="form-control"/>
		<f:field property="moneda" wrapper="bootstrap3"/>
		<f:field property="tc" widget="tc" wrapper="bootstrap3"/>
		<f:field property="descuento" widget="porcentaje" wrapper="bootstrap3"/>
		<f:field property="importe" widget="money" wrapper="bootstrap3"/>
		<f:field property="comentario" wrapper="bootstrap3" widget-class="form-control"/>
		<f:field property="usoCfdi" wrapper="bootstrap3" widget-class="form-control">
			%{-- <g:select class="form-control chosen-select"  
			 	name="usoCfdi" 
			 	from="${['G01','G02']}" 
			 /> --}%
			 <g:select class="form-control chosen-select"  
				name="${property}" 
				value="${'G02'}"
				from="${V32CfdiUtils.getUsosDeCfdi()}" 
				optionKey="clave" 
				optionValue="descripcion"
				required='required'
			/>
		</f:field>
		<f:field property="ventaRelacionada" wrapper="bootstrap3">
			<g:hiddenField id="ventaRelacionada" name="ventaRelacionada.id" value="${value}" />
			<input 
				id="ventaRelacionadaField" 
				type="text" 
				class="form-control" 
				value="${value}" 
				placeholder="Seleccione la venta o nota de cargo relacionada" required>
			</input>
		</f:field>
			
	</f:with>

	
	<script type="text/javascript">
		$(function(){
			$('.date').bootstrapDP({
			    format: 'dd/mm/yyyy',
			    keyboardNavigation: false,
			    forceParse: false,
			    autoclose: true,
			    todayHighlight: true
			});
			$('.chosen-select').chosen();
			$(".numeric").autoNumeric('init',{vMin:'0'},{vMax:'9999'});
			$(".money").autoNumeric('init',{wEmpty:'zero',mRound:'B',aSign: '$'});
			$(".tc").autoNumeric('init',{vMin:'0.0000'});
			$(".porcentaje").autoNumeric('init',{altDec: '%', vMax: '99.99'});

			$('form[name=createForm]').submit(function(e){
			    console.log("Desablidatndo submit button....");

			    var button=$("#saveBtn");
			    button.attr('disabled','disabled')
			    .html('Procesando...');

			    $(".money,.porcentaje,.numeric,.tc",this).each(function(index,element){
			      var val=$(element).val();
			      var name=$(this).attr('name');
			      var newVal=$(this).autoNumeric('get');
			      $(this).val(newVal);
			    });
			    //e.preventDefault(); 
			    return true;
			});
			$("#tipo").change(function(){
				var tipo=$(this).val();
				
				if(tipo=='DESCUENTO'){
					$("#descuento").removeAttr('disabled');
				}else{
					$("#descuento").autoNumeric('set',0.0).attr("disabled",'disabled');
				}
				
				if(tipo=='BONIFICACION'){
					$("#importe").removeAttr('disabled');
				}else{
					$("#importe").autoNumeric('set',0.0).attr('disabled','disabled');
					
					
				}
			});
			$("#tc").autoNumeric('set','1.0')
			.attr("disabled",'disabled');

			$("#moneda").val('MXN').change(function(){
				var mon=$(this).val();
				if(mon=='MXN'){
					$("#tc").autoNumeric('set','1.0')
					.attr("readonly",true);
				}else{
					$("#tc").attr("readonly",false);
				}
			});

			$("#ventaRelacionadaField").autocomplete({
				source:'<g:createLink   action="ventasAsJSONList"/>',
				minLength:1,
				select:function(e,ui){
					console.log('Valor seleccionado: '+ui.item.id);
					$("#ventaRelacionada").val(ui.item.id);

					
				}
			});
		});
	</script>
	
</content>

</body>
</html>
