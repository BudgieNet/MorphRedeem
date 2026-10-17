package dev.morphie.mr;

import com.budgienet.chatutil.ChatUtil;
import java.io.File;
import java.util.UUID;

import dev.morphie.mr.files.Messages;
import dev.morphie.mr.util.CreditConversion;
import dev.morphie.mr.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;

import dev.morphie.mr.menus.RedeemMenu;
import dev.morphie.mr.util.McMMOMethods;
import dev.morphie.mr.util.DataManager;

public class Commands implements CommandExecutor {
	
	private MorphRedeem plugin;
	  
	public Commands(MorphRedeem plugin) {
		this.plugin = plugin;
	}

	// Console and command blocks have no credits, menu or mcMMO profile of their own.
	private boolean isPlayer(CommandSender sender) {
		if (sender instanceof Player) return true;
		ChatUtil.sendMsg(sender, "This command can only be used by players.", false);
		return false;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("morphredeem") || cmd.getName().equalsIgnoreCase("redeem") || cmd.getName().equalsIgnoreCase("mr")) {
			if (args.length == 0) {
				if (!isPlayer(sender)) return true;
				Player player = (Player)sender;
				if (!player.isSleeping()) {
					if (sender.hasPermission("morphredeem.redeem")) {
						try {
							new RedeemMenu(this.plugin).openGUIRedeem(player);
							return true;
						} catch (McMMOPlayerNotFoundException e1) {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("McMMOPlayerNotLoadedMessage")), false);
							return true;
						}
					} else {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
						return true;
					}
				} else {
					return true;
				}
			} else if (args[0].equalsIgnoreCase("help")) {
				if (sender.hasPermission("morphredeem.help")) {
					ChatUtil.sendMsg(sender, "", false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Header")), false);
					ChatUtil.sendMsg(sender, "", false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Help")), false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.MR")), false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.MRSkill")), false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Credits")), false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.CreditsOthers")), false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Send")), false);
					ChatUtil.sendMsg(sender, "", false);
					if (sender.hasPermission("morphredeem.admin") || sender.hasPermission("morphredeem.reload")) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Add")), false);
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Remove")), false);
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Set")), false);
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Reset")), false);
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Reload")), false);
					}
					ChatUtil.sendMsg(sender, "", false);
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Commands.Footer")), false);
					ChatUtil.sendMsg(sender, "", false);
					return true;
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("Credits")) {
				if (args.length == 1) {
					if (!isPlayer(sender)) return true;
					UUID uuid = ((Player) sender).getUniqueId();
					if (sender.hasPermission("morphredeem.credits")) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("PlayerCreditsMessage").replace("%CREDITS%", new DataManager(plugin).getData(uuid, "Credits"))), false);
						return true;
					} else {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
						return true;
					}
				} else if (args.length == 2) {
					if (sender.hasPermission("morphredeem.credits.others")) {
						Player target = null;
						OfflinePlayer offTarget = null;
						if (Bukkit.getPlayer(args[1]) != null) {
							target = Bukkit.getPlayer(args[1]);
							String targetCredits = new DataManager(plugin).getData(target.getUniqueId(), "Credits");
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("OtherPlayerCreditMessage").replace("%PLAYER%", target.getName()).replace("%CREDITS%", targetCredits)), false);
							return true;
						} else {
							offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
							if (getFileExists(offTarget.getUniqueId())) {
								String targetCredits = new DataManager(plugin).getData(offTarget.getUniqueId(), "Credits");
								ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("OtherPlayerCreditMessage").replace("%PLAYER%", offTarget.getName()).replace("%CREDITS%", targetCredits)), false);
								return true;	
							} else {
								ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
								return true;
							}
						}
					} else {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
						return true;
					}
				}
			} else if (args[0].equalsIgnoreCase("Add")) {
				if (sender.hasPermission("morphredeem.admin") || sender.hasPermission("morphredeem.addcredits")) {
					if (args.length != 3) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Add")), false);
						return true;
					}
		        	try {
		        		Integer.parseInt(args[2]);
		            }
		            catch (NumberFormatException e) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Add")), false);
		            	return true;
		            }
		        	int amount = Integer.parseInt(args[2]);
		            if (amount <= 0) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Add")), false);
		            	return true;
		            }
					Player target = null;
					OfflinePlayer offTarget = null;
					UUID targetUUID = null;
					if (Bukkit.getPlayer(args[1]) != null) {
			        	target = Bukkit.getPlayer(args[1]);
			        	targetUUID = target.getUniqueId();
					} else if (checkIfUUID(args[1]) == true) {
			        	targetUUID = UUID.fromString(args[1]);
			        	target = Bukkit.getPlayer(targetUUID);
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, +amount, "Credits", "add");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddSuccessMessage")), false);
			            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddMessage").replace("%CREDITS%", "" + amount)), false);
			            	return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        } else if (checkIfUUID(args[1]) == false && Bukkit.getPlayer(args[1]) == null) {
						offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
						targetUUID = offTarget.getUniqueId();
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, +amount, "Credits", "add");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        }
					new DataManager(plugin).updateData(targetUUID, +amount, "Credits", "add");
		            if (sender == target) {
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            } else {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddSuccessMessage")), false);
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditAddMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            }
		        } else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (sender.hasPermission("morphredeem.admin") || sender.hasPermission("morphredeem.removecredits")) {
					if (args.length != 3) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Remove")), false);
						return true;
					}
			        int amount = Integer.parseInt(args[2]);
		        	try {
		        		Integer.parseInt(args[2]);
		            }
		            catch (NumberFormatException e) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Remove")), false);
		            	return true;
		            }
		            if (amount <= 0) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Remove")), false);
		            	return true;
		            }
					Player target = null;
					OfflinePlayer offTarget = null;
					UUID targetUUID = null;
					int credits = 0;
					if (Bukkit.getPlayer(args[1]) != null) {
			        	target = Bukkit.getPlayer(args[1]);
			        	targetUUID = target.getUniqueId();
			        	credits = Integer.parseInt(new DataManager(plugin).getData(targetUUID, "Credits"));
			            if (amount > credits) {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidOtherPlayerCredits").replace("%PLAYER%", target.getName()).replace("%CREDITS%", "" + credits)), false);
			            	return true; 
			            }
					} else if (checkIfUUID(args[1]) == true) {
			        	targetUUID = UUID.fromString(args[1]);
			        	target = Bukkit.getPlayer(targetUUID);
			        	credits = Integer.parseInt(new DataManager(plugin).getData(targetUUID, "Credits"));
			            if (amount > credits) {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidOtherPlayerCredits").replace("%PLAYER%", target.getName()).replace("%CREDITS%", "" + credits)), false);
			            	return true; 
			            }
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, -amount, "Credits", "remove");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditRemoveSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        } else if (checkIfUUID(args[1]) == false && Bukkit.getPlayer(args[1]) == null) {
						offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
						targetUUID = offTarget.getUniqueId();
						credits = Integer.parseInt(new DataManager(plugin).getData(targetUUID, "Credits"));
			            if (amount > credits) {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidOtherPlayerCredits").replace("%PLAYER%", target.getName()).replace("%CREDITS%", "" + credits)), false);
			            	return true; 
			            }
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, -amount, "Credits", "remove");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditRemoveSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        }
		            if (amount > credits) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidOtherPlayerCredits").replace("%PLAYER%", target.getName()).replace("%CREDITS%", "" + credits)), false);
		            	return true; 
		            }
		            new DataManager(plugin).updateData(targetUUID, -amount, "Credits", "remove");
		            if (sender == target) {
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditRemoveMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            } else {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditRemoveSuccessMessage")), false);
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditRemoveMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            }
		        } else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}	
			} else if (args[0].equalsIgnoreCase("Set")) {
				if (sender.hasPermission("morphredeem.setcredits")) {
					if (args.length != 3) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Set")), false);
						return true;
					}
		        	try {
		        		Integer.parseInt(args[2]);
		            }
		            catch (NumberFormatException e) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Set")), false);
		            	return true;
		            }
		        	int amount = Integer.parseInt(args[2]);
		            if (amount <= 0) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Set")), false);
		            	return true;
		            }
					Player target = null;
					OfflinePlayer offTarget = null;
					UUID targetUUID = null;
					if (Bukkit.getPlayer(args[1]) != null) {
			        	target = Bukkit.getPlayer(args[1]);
			        	targetUUID = target.getUniqueId();
					} else if (checkIfUUID(args[1]) == true) {
			        	targetUUID = UUID.fromString(args[1]);
			        	target = Bukkit.getPlayer(targetUUID);
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, amount, "Credits", "set");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSetSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        } else if (checkIfUUID(args[1]) == false && Bukkit.getPlayer(args[1]) == null) {
						offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
						targetUUID = offTarget.getUniqueId();
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, amount, "Credits", "set");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSetSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        }
					new DataManager(plugin).updateData(targetUUID, amount, "Credits", "set");
		            if (sender == target) {
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSetMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            } else {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSetSuccessMessage")), false);
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSetMessage").replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            }
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("Send")) {
				if (sender.hasPermission("morphredeem.sendcredits")) {
					if (args.length != 3) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Send")), false);
						return true;
					}
		        	try {
		        		Integer.parseInt(args[2]);
		            }
		            catch (NumberFormatException e) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Send")), false);
		            	return true;
		            }
		        	if (!isPlayer(sender)) return true;
		        	Player commandSender = (Player) sender;
		        	int amount = Integer.parseInt(args[2]);
		            if (amount <= 0) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Send")), false);
		            	return true;
		            }
		            int senderCreds = Integer.parseInt(new DataManager(plugin).getData(commandSender.getUniqueId(), "Credits"));
		            if (amount > senderCreds) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidCredits")), false);
		            	return true; 
		            }
					Player target = null;
					OfflinePlayer offTarget = null;
					UUID targetUUID = null;
					if (Bukkit.getPlayer(args[1]) != null) {
			        	target = Bukkit.getPlayer(args[1]);
			        	targetUUID = target.getUniqueId();
			        } else if (Bukkit.getPlayer(args[1]) == null) {
						offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
						targetUUID = offTarget.getUniqueId();
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, +amount, "Credits", "add");
							new DataManager(plugin).updateData(commandSender.getUniqueId(), -amount, "Credits", "remove");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSendSuccessMessage").replace("%TARGET%", offTarget.getName()).replace("%CREDITS%", "" + amount)), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        }
					new DataManager(plugin).updateData(targetUUID, +amount, "Credits", "add");
					new DataManager(plugin).updateData(commandSender.getUniqueId(), -amount, "Credits", "remove");
		            if (sender == target) {
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSendMessage").replace("%SENDER%", sender.getName()).replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            } else {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSendSuccessMessage").replace("%TARGET%", target.getName()).replace("%CREDITS%", "" + amount)), false);
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditSendMessage").replace("%SENDER%", sender.getName()).replace("%CREDITS%", "" + amount)), false);
		            	return true;
		            }
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("Reset")) {
				if (sender.hasPermission("morphredeem.resetcredits")) {
					if (args.length != 2) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("CorrectUsage.Reset")), false);
						return true;
					}
					Player target = null;
					OfflinePlayer offTarget = null;
					UUID targetUUID = null;
					if (Bukkit.getPlayer(args[1]) != null) {
			        	target = Bukkit.getPlayer(args[1]);
			        	targetUUID = target.getUniqueId();
					} else if (checkIfUUID(args[1]) == true) {
			        	targetUUID = UUID.fromString(args[1]);
			        	target = Bukkit.getPlayer(targetUUID);
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, 0, "Credits", "set");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditResetSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        } else if (checkIfUUID(args[1]) == false && Bukkit.getPlayer(args[1]) == null) {
						offTarget = (OfflinePlayer)Bukkit.getServer().getOfflinePlayer(args[1]);
						targetUUID = offTarget.getUniqueId();
						if (getFileExists(targetUUID)) {
							new DataManager(plugin).updateData(targetUUID, 0, "Credits", "set");
			            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditResetSuccessMessage")), false);
							return true;
						} else {
							ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidPlayer")), false);
							return true;
						}
			        }
					new DataManager(plugin).updateData(targetUUID, 0, "Credits", "set");
		            if (sender == target) {
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditResetMessage")), false);
		            	return true;
		            } else {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditResetSuccessMessage")), false);
		            	ChatUtil.sendMsg(target, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("CreditResetMessage")), false);
		            	return true;
		            }
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("morphredeem.admin") || sender.hasPermission("morphredeem.reload")) {
					Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("MorphRedeem");
		            if (plugin != null) {
		            	plugin.reloadConfig();
						this.plugin.reloadMessages();
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("Prefix") + this.plugin.getMessage("ReloadMessage")), false);
		            	return true;
		            }
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
		        }
			} else if (args.length == 2) {
				if (sender.hasPermission("morphredeem.skillcommand")) {
					if (!isPlayer(sender)) return true;
					Player player = (Player)sender;
					UUID uuid = player.getUniqueId();
					String skill = args[0];
			        try {
			        	Integer.parseInt(args[1]);
			        }
			        catch (NumberFormatException e1) {
			        	ChatUtil.sendMsg(player, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidNumber")), false);
			        	return true;
			        }
					int amount2 = Integer.parseInt(args[1]);
					int credits = Integer.parseInt(new DataManager(plugin).getData(uuid, "Credits"));
					if (!new McMMOMethods().doesSkillExist(skill)) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidSkill")), false);
						return true;
					}
					if (amount2 <= 0) {
						ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidNumber")), false);
						return true;
					}
		            if (amount2 > credits) {
		            	ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidCredits")), false);
		            	return true; 
		            }
					if (plugin.getConfig().getBoolean("Settings.mcMMOSkillXP.Enabled")) {
						new CreditConversion(plugin).ApplyCredit("EXPERIENCE", player, amount2, skill);
						return true;
					} else {
						new CreditConversion(plugin).ApplyCredit("LEVEL", player, amount2, skill);
						return true;
					}
				} else {
					ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("NoPermsMessage")), false);
					return true;
				}
			} else {
				ChatUtil.sendMsg(sender, new StringUtils().addColor(this.plugin.getMessage("ErrorPrefix") + this.plugin.getMessage("InvalidArgsMessage")), false);
				return true;
			}
		}
		return false;
	}
	
	public boolean getFileExists(UUID uuid) {
		File playerFile = new File(this.plugin.getDataFolder() + File.separator + "PlayerData", uuid + ".yml");
	    if (!playerFile.exists()) {
	    	return false;
	    }
	    return true;
	  }
	
	public boolean checkIfUUID(String s) {
		try {
			UUID uuid = UUID.fromString(s);
			return true;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
		
	}
}