<center>

# ⚔️ TrialChamberPlus ⚔️

### *Transform Your Trial Chamber Experience!*

[![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=for-the-badge)](https://papermc.io/)
[![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)](LICENSE)

</center>

---

## 🎯 What is TrialChamberPlus?

**TrialChamberPlus** takes Minecraft 1.21's Trial Chambers to the next level! Add custom rewards, competitive leaderboards, dynamic difficulty scaling, and detailed player statistics to make Trial Chambers the ultimate challenge on your server.

Whether you're running a survival server, SMP, or competitive network - this plugin adds depth and replayability to Trial Chambers!

---

## ✨ Features

### 🎁 Custom Rewards System
- Extra loot drops when defeating Trial Chamber mobs
- Configurable drop chances and amounts
- Bonus XP from Trial Spawners
- Special rewards from Vault blocks
- Permission-based bonus multipliers

### ⚔️ Dynamic Difficulty Scaling
- Mobs get stronger with more players nearby
- Configurable health and damage multipliers
- Special Breeze enhancements
- Visual indicators for scaled mobs (glowing effect)
- Maximum difficulty cap to prevent impossibility

### 🏆 Leaderboard System
- Track player points and rankings
- Compete with other players
- Multiple point sources:
  - Mob kills
  - Breeze kills
  - Vault opens
  - Chamber completions

### 📊 Detailed Statistics
- Personal stats tracking
- Mob kill counters
- Breeze kill counters
- Vaults opened
- Chambers completed
- Global ranking position

### 📢 Server Announcements
- Broadcast when players enter Trial Chambers
- Announce chamber completions
- Fully customizable messages
- Toggle on/off per feature

---

## 📋 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/trial help` | Show help menu | `trialchamberplus.use` |
| `/trial stats` | View your personal statistics | `trialchamberplus.stats` |
| `/trial leaderboard` | View top players | `trialchamberplus.leaderboard` |
| `/trial reload` | Reload configuration | `trialchamberplus.admin` |
| `/trial addpoints <player> <amount>` | Add points to player | `trialchamberplus.admin` |

**Aliases:** `/tc`, `/trialchamber`

---

## 🔐 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `trialchamberplus.use` | Basic plugin usage | Everyone |
| `trialchamberplus.stats` | View personal stats | Everyone |
| `trialchamberplus.leaderboard` | View leaderboard | Everyone |
| `trialchamberplus.admin` | Admin commands & reload | OP |
| `trialchamberplus.rewards.bonus` | 1.5x reward multiplier | OP |

---

## ⚙️ Configuration

TrialChamberPlus is **highly configurable**! Customize everything:

```yaml
# Custom Rewards
rewards:
  bonus-xp: 50
  custom-drops:
    - material: DIAMOND
      chance: 0.1
      min-amount: 1
      max-amount: 3
    - material: MACE
      chance: 0.02
      min-amount: 1
      max-amount: 1

# Difficulty Scaling
difficulty:
  scale-with-players: true
  health-multiplier-per-player: 0.25
  max-multiplier: 3.0

# Points System
leaderboard:
  points:
    mob-kill: 10
    breeze-kill: 25
    vault-open: 50
    chamber-complete: 100
