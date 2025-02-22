package eu.decentsoftware.holograms.plugin.commands;

import com.google.common.collect.Lists;
import eu.decentsoftware.holograms.api.Lang;
import eu.decentsoftware.holograms.api.commands.*;
import eu.decentsoftware.holograms.api.convertor.IConvertor;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.utils.Common;
import eu.decentsoftware.holograms.api.utils.message.Message;
import eu.decentsoftware.holograms.api.utils.scheduler.S;
import eu.decentsoftware.holograms.plugin.Validator;
import eu.decentsoftware.holograms.plugin.convertors.ConvertorResult;
import eu.decentsoftware.holograms.plugin.convertors.ConvertorType;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@CommandInfo(
		aliases = {"holograms", "hologram", "dh", "holo"},
		permissions = {"dh.default", "dh.command.decentholograms"},
		usage = "/dh <args>",
		description = "DecentHolograms 主命令."
)
public class HologramsCommand extends DecentCommand {

	public HologramsCommand() {
		super("decentholograms");

		addSubCommand(new HelpSubCommand());
		addSubCommand(new ReloadSubCommand());
		addSubCommand(new ListSubCommand());
		addSubCommand(new HologramSubCommand());
		addSubCommand(new LineSubCommand());
		addSubCommand(new FeatureSubCommand());
		addSubCommand(new PageSubCommand());
		addSubCommand(new ConvertSubCommand());
        addSubCommand(new VersionSubCommand());
//        addSubCommand(new TestSubCommand());

        // Shortcuts
        addSubCommand(new HologramSubCommand.HologramCreateSub());
        addSubCommand(new HologramSubCommand.HologramDeleteSub());
        addSubCommand(new HologramSubCommand.HologramCloneSub());
        addSubCommand(new HologramSubCommand.HologramEnableSub());
        addSubCommand(new HologramSubCommand.HologramDisableSub());
        addSubCommand(new HologramSubCommand.HologramAlignSub());
        addSubCommand(new HologramSubCommand.HologramCenterSub());
        addSubCommand(new HologramSubCommand.HologramInfoSub());
        addSubCommand(new HologramSubCommand.HologramNearSub());
        addSubCommand(new HologramSubCommand.HologramTeleportSub());
        addSubCommand(new HologramSubCommand.HologramMoveSub());
        addSubCommand(new HologramSubCommand.HologramMovehereSub());
	}

	@Override
	public CommandHandler getCommandHandler() {
		return (sender, args) -> {
			if (sender.hasPermission("dh.admin")) {
				if (args.length == 0) {
					Lang.USE_HELP.send(sender);
					return true;
				}
				Lang.UNKNOWN_SUB_COMMAND.send(sender);
				Lang.USE_HELP.send(sender);
			} else {
                Lang.sendVersionMessage(sender);
            }
			return true;
		};
	}

	@Override
	public TabCompleteHandler getTabCompleteHandler() {
		return null;
	}

    /*
     *  SubCommands
     */

    @CommandInfo(
            permissions = "dh.command.test",
            usage = "/dh test",
            playerOnly = true,
            minArgs = 1,
            description = "测试命令."
    )
    public static class TestSubCommand extends DecentCommand {

        public TestSubCommand() {
            super("test");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                // Nothing
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }
    }

    @CommandInfo(
            permissions = {"dh.default", "dh.command.version"},
            usage = "/dh version",
            aliases = {"ver", "about"},
            description = "打印 DecentHolograms 的版本信息."
    )
    public static class VersionSubCommand extends DecentCommand {

        public VersionSubCommand() {
            super("version");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                Lang.sendVersionMessage(sender);
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }
    }

    @CommandInfo(
            permissions = "dh.command.reload",
            usage = "/dh reload",
            description = "重载插件."
    )
    public static class ReloadSubCommand extends DecentCommand {

        public ReloadSubCommand() {
            super("reload");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                S.async(() -> {
                    long start = System.currentTimeMillis();
                    PLUGIN.reload();
                    long end = System.currentTimeMillis();
                    Lang.RELOADED.send(sender, end - start);
                });
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }

    }

    @CommandInfo(
            permissions = "dh.command.list",
            usage = "/dh list [page]",
            description = "列出悬浮字文件中载入的所有悬浮字.",
            playerOnly = true
    )
    public static class ListSubCommand extends DecentCommand {

        public ListSubCommand() {
            super("list");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                final List<Hologram> holograms = Lists.newArrayList(PLUGIN.getHologramManager().getHolograms());
                if (holograms.isEmpty()) {
                    Common.tell(sender, "%s当前没有悬浮字.", Common.PREFIX);
                    return true;
                }
                int currentPage = args.length >= 1 ? Validator.getInteger(args[0], "页面必须是有效整数.") - 1 : 0;
                List<String> header = Lists.newArrayList("", " &3&lHOLOGRAM LIST - #" + (currentPage + 1), " &f已创建的悬浮字：", "");
                Function<Hologram, String> parseItem = hologram -> {
                    Location l = hologram.getLocation();
                    String name = (hologram.isEnabled() ? "" : "&c") + hologram.getName();
                    return String.format(" &8• &b%s &8| &7%s, %.2f, %.2f, %.2f", name, l.getWorld().getName(), l.getX(), l.getY(), l.getZ());
                };
                Message.sendPaginatedMessage((Player) sender, currentPage, "/dh list %d", 15, header, null, holograms, parseItem);
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return (sender, args) -> {
                if (args.length != 1) {
                    return null;
                }

                int holograms = PLUGIN.getHologramManager().getHolograms().size();
                if (holograms == 0) {
                    return null;
                }

                List<String> pages = new ArrayList<>();
                int page = 0;
                while(holograms > 0) {
                    page++;
                    pages.add(String.valueOf(page));
                    holograms -= 15;
                }

                return TabCompleteHandler.getPartialMatches(args[0], pages);
            };
        }

    }

    @CommandInfo(
            permissions = "dh.command.help",
            usage = "/dh help",
            description = "显示帮助页面.",
            aliases = {"?"}
    )
    public static class HelpSubCommand extends DecentCommand {

        public HelpSubCommand() {
            super("help");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                sender.sendMessage("");
                Common.tell(sender, " &3&lDECENT HOLOGRAMS HELP");
                Common.tell(sender, " 所有主命令.");
                sender.sendMessage("");
                CommandBase command = PLUGIN.getCommandManager().getMainCommand();
                List<CommandBase> subCommands = Lists.newArrayList(command.getSubCommands());
                for (CommandBase subCommand : subCommands) {
                    if (subCommand.getClass().toString().contains("HologramSubCommand")) continue;
                    Common.tell(sender, " &8• &b%s &8- &7%s", subCommand.getUsage(), subCommand.getDescription());
                }
                sender.sendMessage("");
                Common.tell(sender, " &7别名: &b%s%s",
                        command.getName(),
                        command.getAliases().size() > 1
                                ? ", " + String.join(", ", command.getAliases())
                                : ""
                );
                sender.sendMessage("");
                return true;
            };
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return null;
        }

    }

    @CommandInfo(
            permissions = "dh.command.convert",
            usage = "/dh convert <plugin> [file]",
            description = "从其他悬浮字插件导入悬浮字数据.",
            minArgs = 1
    )
    public static class ConvertSubCommand extends DecentCommand {

        public ConvertSubCommand() {
            super("convert");
        }

        @Override
        public CommandHandler getCommandHandler() {
            return (sender, args) -> {
                final ConvertorType convertorType = ConvertorType.fromString(args[0]);
                final String path = args.length >= 2 ? args[0] : null;
                if (convertorType == null) {
                    Common.tell(sender, "%s&c无法转换! 未知的插件 '%s'", Common.PREFIX, args[0]);
                    return true;
                }

                long startTime = System.currentTimeMillis();
                IConvertor convertor = convertorType.getConvertor();
                if (convertor == null) {
                    Common.tell(sender, "%s&c无法转换! 未知的插件 '%s'", Common.PREFIX, args[0]);
                    return true;
                }

                Common.tell(sender, "%s正在从 %s 转换", Common.PREFIX, convertorType.getName());
                if (convertorType.isLimited()) {
                    Common.tell(sender, "%s&6NOTE: %s 支持不完全!", Common.PREFIX, convertorType.getName());
                }

                ConvertorResult result;
                if (path != null) {
                    File file = new File(path);
                    result = convertor.convert(file);
                } else {
                    result = convertor.convert();
                }
                sendResult(sender, startTime, result);
                return true;
            };
        }

        public void sendResult(CommandSender sender, long startTime, ConvertorResult result) {
            Common.tell(sender, "%s 在 %s ms 转换 %d 个全息图!", Common.PREFIX, result.getTotalCount(), System.currentTimeMillis() - startTime);
            Common.tell(sender, "%s- &a%d 成功", Common.PREFIX, result.getSuccessCount());
            Common.tell(sender, "%s- &e%d 跳过", Common.PREFIX, result.getSkippedCount());
            Common.tell(sender, "%s- &c%d 失败", Common.PREFIX, result.getFailedCount());
        }

        @Override
        public TabCompleteHandler getTabCompleteHandler() {
            return (sender, args) -> {
                if (args.length == 1) {
                    return TabCompleteHandler.getPartialMatches(args[0], Arrays.stream(ConvertorType.values()).map(ConvertorType::getName).collect(Collectors.toList()));
                }
                return null;
            };
        }

    }
}
