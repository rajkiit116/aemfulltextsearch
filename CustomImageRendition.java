

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import  org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import org.apache.sling.api.resource.LoginException;

import com.day.cq.dam.api.handler.AssetHandler;
import com.day.cq.dam.api.renditions.RenditionMaker;
import com.day.cq.dam.api.renditions.RenditionTemplate;
import com.day.cq.dam.api.thumbnail.ThumbnailConfig;


@Component(service = WorkflowProcess.class, property = {"process.label = Custom Image Rendition Hai"})
public class CustomImageRendition implements WorkflowProcess {
	private static final Logger logger = LoggerFactory.getLogger(CustomImageRendition.class.getName());
	@Reference
	protected ResourceResolverFactory resourceResolverFactory;
	@Reference
	RenditionMaker renditionMaker;
	@Reference
	AssetHandler assetHandler;
	ResourceResolver resourceResolver;

	@Override  
	public void execute(WorkItem workItem, WorkflowSession session, MetaDataMap args) throws WorkflowException { 
		try      {    
			String assetPath = null;    // Get the resource resolver using system user and user mapper service      
			Map<String, Object> param = new HashMap<String, Object>();      
			param.put(ResourceResolverFactory.SUBSERVICE, "getResourceResolver");     
			resourceResolver = resourceResolverFactory.getServiceResourceResolver(param);
     
			String payloadString = workItem.getWorkflowData().getPayload().toString();     
			logger.info("payload path"+payloadString);             // convert the payload path into a Resource    
			logger.info("Hello");
			Resource damResource = resourceResolver.resolve(payloadString);    
			logger.info(" check dam resource  "+damResource);
		if (damResource != null){       
			logger.info("the damResource is exists .. "+damResource);                // further convert the resource into Dam asset       
			Asset damAsset =  damResource.adaptTo(Asset.class);       
			if(damAsset !=null)          {    
				logger.info("dam asset exists .. "+ damAsset);                // create a Rendition Template using Rendition Maker Api and give the width, height, quality, mimietype for your template
				int width = 250;        
				int height = 250;       
				int quality = 100;         
				String mimeType = "image/jpeg";      
				String[] mimeTypesToKeep ={ "image/jpeg","image/png"};
				
				RenditionTemplate renditionTemplate = renditionMaker.createWebRenditionTemplate(damAsset, width, height, quality,mimeType,mimeTypesToKeep);                        // Using the rendition template created above , generate the renditions        
				List<com.day.cq.dam.api.Rendition> renditionList = renditionMaker.generateRenditions(damAsset, renditionTemplate);               // using Asset Handler Api create thumbnails using the rendition for the asset.      
				Collection<ThumbnailConfig> configs = null;        
					for (com.day.cq.dam.api.Rendition rendition : renditionList) {
						assetHandler.createThumbnails(damAsset, rendition, configs);
					}                // Just to check if our rendition got added.     
					
					for (com.day.cq.dam.api.Rendition rendition2 : damAsset.getRenditions()) {          
						logger.info(rendition2.getName() + " " + rendition2.getPath()+"\n");      
						}               
					
				resourceResolver.commit();  
				}       
					}      
				}  catch(LoginException | IOException e){  e.printStackTrace();  
				}
				
				}

		
}