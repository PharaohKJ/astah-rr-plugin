package xyz.dskow.astah_rr_plugin;

import static java.awt.EventQueue.invokeLater;
import lombok.extern.slf4j.Slf4j;
import org.osgi.framework.BundleActivator;

import org.osgi.framework.BundleContext;
import xyz.dskow.astah_rr_plugin.model.astahUtils.AstahApiWrapper;
import xyz.dskow.astah_rr_plugin.model.CodeViewApp;

@Slf4j
public class Activator implements BundleActivator {

	private static CodeViewApp APP;

	public static CodeViewApp getApp() {
		return APP;
	}

	@Override
	public void start(BundleContext context) {
		invokeLater(() -> {
			APP = new CodeViewApp();
			AstahApiWrapper.getViewManager().getDiagramViewManager().addEntitySelectionListener(APP);
			AstahApiWrapper.getProjectAccessor().addProjectEventListener(APP);
		});
	}

	@Override
	public void stop(BundleContext context) {
	}
}
