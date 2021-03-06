<%@ page import="com.luxsoft.impapx.tesoreria.PagoProveedor" %>
<!doctype html>
<html>
<head>
	<title>Saldo de cuentas contables</title>
	<meta name="layout" content="luxor">
</head>
<body>

<content tag="header">
	Saldo de cuentas contables Periodo: ${session.periodoContable}
</content>
	
<content tag="subHeader">
	
</content>

<content tag="document">
	<div class="row">
	    <div class="col-lg-12">
	        <div class="ibox float-e-margins">
	        	
	        	<div class="ibox-title">
	        		 <button data-target="#periodoDialog" data-toggle="modal" class="btn btn-outline btn-success  dim">
	        		 	<i class="fa fa-calendar"></i> 
	        		</button>
	        		<lx:refreshButton/>
	        		<div class="btn-group">
	        		    <button type="button" name="operaciones"
	        		            class="btn btn-info dropdown-toggle" data-toggle="dropdown"
	        		            role="menu">
	        		            Reportes <span class="caret"></span>
	        		    </button>
	        		    <ul class="dropdown-menu">
	        		    	<li>
    		    	     		<g:jasperReport
    		    	     			jasper="BalanzaDeComprobacion" 
    		    	     			format="PDF,HTML" 
    		    	     			name="Balanza"
    		    	     			buttonPosition="top">
	    	    						<g:hiddenField name="YEAR" value="${session.periodoContable.ejercicio}"/>
	    	    						<g:hiddenField name="MES" value="${session.periodoContable.mes}"/>
    		    	    		</g:jasperReport>
	        		    	</li>
    		    	 		<li>
    		    	 			<g:jasperReport
    		    	 			jasper="BalanceGeneral" 
    		    	 			format="PDF" 
    		    	 			name="Balance general"
    		    	 			buttonPosition="top">
	    							<g:hiddenField name="YEAR" value="${session.periodoContable.ejercicio}"/>
	    							<g:hiddenField name="MES" value="${session.periodoContable.mes}"/>
    		    				</g:jasperReport>
    		    	 		</li>
	        		    	 		
    		    	 		<li>
    		    	 			<g:jasperReport
    		    	 			jasper="EstadoDeResultados" 
    		    	 			format="PDF" 
    		    	 			name="Estado de resultados"
    		    	 			buttonPosition="top">
	    							<g:hiddenField name="YEAR" value="${session.periodoContable.ejercicio}"/>
	    							<g:hiddenField name="MES" value="${session.periodoContable.mes}"/>
    		    				</g:jasperReport>
    		    	 		</li>
	        		    </ul>
	        		</div>
	        		<div class="btn-group">
	        		    <button type="button" name="operaciones"
	        		            class="btn btn-default dropdown-toggle" data-toggle="dropdown"
	        		            role="menu">
	        		            Operaciones <span class="caret"></span>
	        		    </button>
	        		    <ul class="dropdown-menu">
	        		    	<g:if test="${session.periodoContable.mes!=13}">
    		    		 		<li>
    		    		 			<g:link action="actualizarSaldos" onclick="return confirm('Actualiar saldos?');">
    		    		 				Actualizar saldos
    		    		 			</g:link>
    		    				</li>
	        		    	</g:if>
    		    			<g:elseif test="${session.periodoContable.mes==13}">
    		    				 		<li>
    		    				 			<g:link action="generarCierreAnual" 
    		    				 				onclick="return confirm('Generar cierre anual?');">
    		    				 				Generar cierre
    		    				 			</g:link>
    		    						</li>
    		    						<li>
    		    				 			<g:link action="actualizarCierreAnual" 
    		    				 				onclick="return confirm('Actualizar cierre anual?');">
    		    				 				Actualizar cierre anual
    		    				 			</g:link>
    		    						</li>
    		    						<li>
    		    				 			<g:link action="eliminarCierreAnual" 
    		    				 				onclick="return confirm('Eliminar cierre anual '+${session.periodoContable.ejercicio})+ '?';">
    		    				 				Eliminar cierre anual
    		    				 			</g:link>
    		    						</li>
    		    			</g:elseif>
    		    			
	        		    </ul>
	        		</div>
	        	    <div class="ibox-tools">
	        	        <a class="collapse-link">
	        	            <i class="fa fa-chevron-up"></i>
	        	        </a>
	        	        <a class="close-link">
	        	            <i class="fa fa-times"></i>
	        	        </a>
	        	    </div>
	        	</div>
	            <div class="ibox-content">
	            	<table id="grid" class="grid table table-responsive table-striped table-bordered table-hover">
	            		<thead>
	            			<tr>
	            				<th>Cuenta</th>
	            				<th>Descripción</th>
	            				<th>Saldo inicial</th>
	            				<th>Debe</th>
	            				<th>Haber</th>
	            				<th>Saldo final</th>
	            				<th>Año</th>
	            				<th>Mes</th>
	            				<th>Modificado</th>
	            			</tr>
	            		</thead>
	            		<tbody>
	            			<g:each in="${saldoPorCuentaContableInstanceList}" var="row">
	            				<tr id="${row.id}">
	            					<td><g:link action="subcuentas" id="${row.id}" >
	            							${fieldValue(bean: row, field: "cuenta.clave")}
	            						</g:link>
	            					</td>
	            					<td>${fieldValue(bean: row, field: "cuenta.descripcion")}</td>
	            					<td><lx:moneyFormat number="${row.saldoInicial}"/></td>
	            					<td><lx:moneyFormat number="${row.debe}"/></td>
	            					<td><lx:moneyFormat number="${row.haber}"/></td>
	            					<td><lx:moneyFormat number="${row.saldoFinal}"/></td>
	            					<td><g:formatNumber number="${row.year}" format="####"/></td>
	            					<td><g:formatNumber number="${row.mes}"  format="##"/></td>
	            					<td><g:formatDate date="${row.lastUpdated}"/></td>
	            				</tr>
	            			</g:each>
	            		</tbody>
	            	</table>
	            </div>
	        </div>
	    </div>
	</div>

	<g:render template="/common/selectorDePeriodoContable" bean="${session.periodoContable}"/>
	
	%{-- <div class="modal fade" id="periodoDialog" tabindex="-1">
		<div class="modal-dialog ">
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h4 class="modal-title" id="myModalLabel">Cambiar periodo</h4>
				</div>
				<g:form action="cambiarPeriodo" class="form-horizontal" >
					<div class="modal-body">
						<div class="form-group" id="data_4">
                            <label class="font-noraml">Seleccionar mes</label>
                            <div class="input-group date">
                                <span class="input-group-addon"><i class="fa fa-calendar"></i></span>
                                <input type="text" class="form-control" name="fecha"
                                	value="${formatDate(date:session.periodoContable,format:'dd/MM/yyyy')}">
                            </div>
						</div>
					</div>
					
					<div class="modal-footer">
						<button type="button" class="btn btn-default" data-dismiss="modal">Cerrar</button>
						<g:submitButton class="btn btn-info" name="aceptar"
								value="Aceptar" />
					</div>
				</g:form>
	
			</div>
			<!-- moda-content -->
		</div>
		<!-- modal-di -->
	</div> --}%
	
	
	<script type="text/javascript">
		$(function(){
 			$('#grid').dataTable({
                responsive: true,
                aLengthMenu: [[100, 150, 200, 250, -1], [100, 150, 200, 250, "Todos"]],
                "language": {
					"url": "${assetPath(src: 'datatables/dataTables.spanish.txt')}"
	    		},
	    		"dom": 'T<"clear">lfrtip',
	    		"tableTools": {
	    		    "sSwfPath": "${assetPath(src: 'plugins/dataTables/swf/copy_csv_xls_pdf.swf')}"
	    		},
	    		"order": []
            });

            // $('#data_4 .input-group.date').bootstrapDP({
            //     minViewMode: 1,
            //     format: 'dd/mm/yyyy',
            //     keyboardNavigation: false,
            //     forceParse: false,
            //     autoclose: true,
            //     todayHighlight: true,

            // });

		});
	</script>
	
</content>
	
</body>
</html>
