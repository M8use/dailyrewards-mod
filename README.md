# Daily Rewards

**Log in every day, keep your streak alive, and watch the rewards get better and better.
**
**Daily Rewards** is a lightweight, standalone reward calendar for Minecraft. Every real-world day you log in, you can claim a randomized reward — anything from a handful of common materials up to genuinely rare, fully enchanted gear — with better odds, streak bonuses, and dedicated weekly and milestone jackpots the longer you keep coming back.

It's built to sit quietly in your inventory until you need it: one button, one calendar, zero clutter, no config required. **Made with claude mainly by m8use and mimikyu helped with some of the coding errors :))**

**Available on NeoForge (1.21.1)**

---

## What You Get

- **A calendar in your pocket.** A small button lives right in your inventory screen. Click it any time to open the calendar — no commands to remember.
- **A real 7-day week, always.** Six regular days plus one big featured day, always showing where you currently are, refreshed automatically at midnight your server's time.
- **Rewards that actually scale.** Early days lean toward common materials; by day 30 you're seeing genuine Rare/Epic variety. Every 7th day is a guaranteed Epic or Legendary reward.
- **Milestone jackpots.** Every 50th day is a guaranteed Mythic-tier reward — think stacks of netherite, totems of undying, an elytra, or a fully enchanted top-tier weapon.
- **Streaks that matter.** Log in on consecutive real days to build a streak. A longer streak gives you:
  - A better chance your reward gets bumped up a full rarity tier
  - Bonus quantity on stackable rewards
  - Extra bonus items at streak milestones (7, 14, 30, 50, and 100 days)
- **A little suspense.** Locked and available days flip through real items pulled straight from the loot table — including the rare stuff — so you always get a sense of what's possible without ever knowing exactly what's coming.
- **A record of your week.** Once a day is claimed, the calendar keeps showing exactly what you got and how much for the rest of the week — no guessing what you already grabbed.

## ✨ Presentation

We didn't want this to feel like a spreadsheet:

- A rotating "solar flare" on the inventory button lets you know at a glance when a reward is waiting — and disappears the moment you've claimed it
- A colorful, cartoon-styled calendar with animated sunburst backgrounds behind every card, and a spiraling rainbow behind the featured 7th-day reward
- A streak flame that flares up with its own glow every time you extend your streak
- Confetti and a rain of the actual item you just won, every time you claim — bigger and louder for weekly and milestone rewards
- A live countdown to your next reward right in the header, which turns into a moving rainbow once you're under an hour away

![In Game](https://cdn.modrinth.com/data/cached_images/ed8ab3a8364bf59e8fd061af15f773616ac852e7.jpeg)

## 🔒 Fair By Design

Everything that matters is decided and checked on the server, never the client:

- "Today" is based on the server's real calendar date — never Minecraft playtime, ticks, or your system clock
- Claims are validated and locked in server-side, so there's no way to trick the game into handing out the same reward twice
- Nothing about a given day's reward is decided — or knowable — before the moment you actually claim it

## 🧪 For Server Admins

A small set of testing commands, gated behind the same permission level as `/give` or `/gamemode` (so only ops or cheats-enabled worlds can use them):

- `/dailyrewards skip <days>` — fast-forward without waiting on real time
- `/dailyrewards setday <day> [streak]` — jump straight to a specific day and streak, handy for previewing the weekly or day-50 milestone reward
- `/dailyrewards reset` — wipe your progress and re-trigger the first-time welcome popup
- `/dailyrewards status` — print your current day count, streak, and claim status

## 📥 Installation Guide

1. Install NeoForge for Minecraft 1.21.1.
2. Drop the Daily Rewards `.jar` into your `mods` folder.
3. Launch the game. That's it — nothing to configure.

The very first time you create and join a new world, the calendar pops up automatically to introduce itself and let you claim Day 1. After that, just open it from the inventory button whenever you're ready to check in.

## ⚙️ Compatibility

Daily Rewards is fully standalone — it has no dependency on any other mod and doesn't require anything else installed. Its inventory button is also positioned to sit neatly alongside other inventory-button mods rather than overlapping them.

- **Mod loader:** NeoForge
- **Minecraft version:** 1.21.1
- **Client & Server:** required on both — this is a server-authoritative system by design

## 🐛 How to Report Issues

Found a bug, a visual glitch, or a reward that feels off? [Add your issue tracker / Discord / contact link here] — please include your Minecraft and NeoForge version, and a screenshot or log file if you can.
