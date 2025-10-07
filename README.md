<p align="center">
  <a href="https://www.haagensen.me">
    <img
      src="https://cdn.modrinth.com/data/cached_images/b811a708d2a8f791cf233906b023325b01812d2e.png"
      alt="haagensen.me"
      width="750"
      loading="lazy"
    />
  </a>
</p>

---

## 🎭 Player Head Display

- **Player Skins**  
  Displays each player’s skin instead of the default waypoint icon.
- **Distance-Based Scaling**  
  Heads scale dynamically with distance—just like vanilla waypoints.
- **Resizable Heads**  
  Adjust the size of player heads to your preference.

<div align="center">
  <img
    src="https://cdn.modrinth.com/data/cached_images/47abf66f7bc76e0829011f30e4b9c8dff5534d58.png"
    alt="Player Heads"
    width="700"
  />
</div>

---

## 🎨 Team Border System

- **Customizable Borders**  
  Add colored outlines around player heads based on their team colour.
- **Border Thickness**  
  Choose from four presets:
  - `Thin (0.3px)`
  - `Medium (0.5px)`
  - `Normal (1.0px)` _(default)_
  - `Thick (2.0px)`
- **Automatic Colour Detection**  
  Pulls team colours directly from the scoreboard.
- **Toggleable**  
  Enable or disable borders as needed.

<div align="center">
  <img
    src="https://cdn.modrinth.com/data/cached_images/034086bbf3454d124e3b3485b9af49f3c2f167dd.png"
    alt="Team Borders"
    width="700"
  />
</div>

---

## 📝 Player Name Display

Control when player names appear above heads (within simulation range):

| Option         | Description                                                      |
| -------------- | ---------------------------------------------------------------- |
| **Never**      | _(default)_ No names on the locator bar.                         |
| **Always**     | Names permanently visible above all player heads.                |
| **Looking At** | Names appear only when you look within a 30° cone at a player.   |
| **Player List**| Names show when the in-game player list is open.                 |

**Name Colouring**  
If team borders are enabled, names will adopt the corresponding team colour automatically.

<div align="center">
  <img
    src="https://cdn.modrinth.com/data/cached_images/1909f85865dee0167f2a8097888227b99323b9f0.png"
    alt="Player Names"
    width="700"
  />
</div>

---

## 🎯 Advanced Player Filtering

choose whose heads appear on your locator bar, both heads and vanilla waypoints:

- **All Players** _(default)_  
  Show everyone.
- **Include Only**  
  Display only the players listed in your filter.
- **Exclude Only**  
  Hide only the players listed, showing all others.

**Filter Syntax**  
- Separate names by commas (`,`), semicolons (`;`), colons (`:`), or periods (`.`).  

> **Example:**  
> ``Jeb_, Dinnerbone; LadyAgnes``

---

## 🔗 Dependencies

### Required

<a href="https://modrinth.com/mod/fabric-api">
  <img
    src="https://cdn.modrinth.com/data/cached_images/b541ef52cf8c8e8d990ac261f745a0d405896096.png"
    alt="Fabric API"
    width="80"
    loading="lazy"
  />
</a>  

<a href="https://modrinth.com/mod/cloth-config">
  <img
    src="https://cdn.modrinth.com/data/cached_images/88b60d015c0aa162a0143002060268947969c975_0.webp"
    alt="Cloth Config API"
    width="80"
    loading="lazy"
  />
</a>  

### Optional (Recommended)

<a href="https://modrinth.com/mod/modmenu">
  <img
    src="https://cdn.modrinth.com/data/cached_images/09f9500c73623d8ee3d3608dba843f86e94c1f5e_0.webp"
    alt="Mod Menu"
    width="80"
    loading="lazy"
  />
</a>  

---

## 👥 Credits

- **Original Head Rendering Concept & Code:** [MCRcortex](https://github.com/MCRcortex)  
