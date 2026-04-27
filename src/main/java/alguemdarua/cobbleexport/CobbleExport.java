package alguemdarua.cobbleexport;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CobbleExport.MOD_ID, dist = Dist.CLIENT)
public final class CobbleExport {
    public static final String MOD_ID = "cobbleexport";

    public CobbleExport() {
        NeoForge.EVENT_BUS.addListener(ExportCommand::register);
    }
}
