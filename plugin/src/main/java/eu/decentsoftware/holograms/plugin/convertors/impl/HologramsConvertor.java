package eu.decentsoftware.holograms.plugin.convertors.impl;

import eu.decentsoftware.holograms.api.DecentHolograms;
import eu.decentsoftware.holograms.api.DecentHologramsAPI;
import eu.decentsoftware.holograms.api.convertor.IConvertor;
import eu.decentsoftware.holograms.api.utils.Log;
import eu.decentsoftware.holograms.api.utils.config.FileConfig;
import eu.decentsoftware.holograms.api.utils.location.LocationUtils;
import eu.decentsoftware.holograms.plugin.convertors.ConverterCommon;
import eu.decentsoftware.holograms.plugin.convertors.ConvertorResult;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class HologramsConvertor implements IConvertor {

    private static final DecentHolograms PLUGIN = DecentHologramsAPI.get();

    @Override
    public ConvertorResult convert() {
        return convert(new File(PLUGIN.getDataFolder().getParent() + "/Holograms/", "holograms.yml"));
    }

    @Override
    public ConvertorResult convert(final File file) {
        Log.info("正在转换 Holograms 悬浮字...");
        if (ConverterCommon.notValidFile(file, "holograms.yml")) {
            Log.warn("无效文件！需要 'holograms.yml'");
            return ConvertorResult.createFailed();
        }

        FileConfig config = new FileConfig(PLUGIN.getPlugin(), file);
        ConvertorResult convertorResult = new ConvertorResult();

        ConfigurationSection hologramsSection = config.getConfigurationSection("holograms");
        if (hologramsSection != null) {
            for (String id : hologramsSection.getKeys(false)) {
                Location location = LocationUtils.asLocation(hologramsSection.getString(id + ".location"), ";");
                if (location == null) {
                    Log.warn("无法转换 '%s'！位置无效。", id);
                    convertorResult.addFailed();
                    continue;
                }

                List<String> lines = prepareLines(hologramsSection.getStringList(id + ".lines"));
                ConverterCommon.createHologram(convertorResult, id, location, lines, PLUGIN);
            }
        }
        return convertorResult;
    }

    @Override
    public List<String> prepareLines(List<String> lines) {
        return lines.stream().map(line -> {
            if (line.toUpperCase().startsWith("ITEM:")) {
                return "#ICON" + line.substring(4);
            }
            return line;
        }).collect(Collectors.toList());
    }

}
