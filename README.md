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

- **Real Player Skins**  
  Shows player skins instead of generic waypoint icons.  
- **Hat Layer Support**  
  Displays both the base skin layer and hat/helmet overlay for complete skin representation.  
- **Distance-Based Scaling**  
  Player heads scale naturally with distance, just like vanilla waypoints.

---

## 🎨 Team Border System

- **Configurable Team Borders**  
  Adds coloured borders around player heads based on their team colour.  
- **Multiple Border Thickness Options**  
  - **Thin (0.3px)**  
  - **Medium (0.5px)**  
  - **Normal (1.0px)** _Default_  
  - **Thick (2.0px)**  
- **Colour Detection**  
  Automatically detects team colours  from the scoreboard.  
- **Toggle Option**  
  Can be completely disabled if you prefer clean heads without borders.

<p align="center">
  <img
    src="https://cdn.modrinth.com/data/cached_images/9f851cbcc07134f303422e683a5c4531c923c8e7.png"
    alt="Player head example"
    width="300"
  />
</p>

---

## 📝 Player Name Display

Choose when to show player names above their heads, when within simulation distance:
  - **Never:** _Default_ <br>
    No names on locator bar  
  - **Always:** <br>
    Names permanently visible above all player heads
  - **Looking At:** <br>
    Names only appear when you're looking in the direction of a player (within a 30° cone.

---

## 🎯 Advanced Player Filtering

Control which players heads appear in your locator bar:

- **All Players**  
  Show everyone. _Default_  
- **Include Only**  
  Show only specific players from your custom list.  
- **Exclude Only**  
  Hide specific players while showing everyone else.

(excluded players appear normally on the locator bar as a coloured dot)

### 🔧 Filter Configuration

- **Comma-Separated Lists**  
  Enter player names separated by commas, semicolons, colons, or periods.  
- **Non Case-Insensitive**  
  Player name matching ignores capitalization.  
- **Example**  
  ``Player1, Player2, Player3`` or ``Notch; Dinnerbone: Grumm``

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

### Optional (Recommended)

<a href="https://modrinth.com/mod/cloth-config">
  <img
    src="https://cdn.modrinth.com/data/cached_images/88b60d015c0aa162a0143002060268947969c975_0.webp"
    alt="Cloth Config API"
    width="80"
    loading="lazy"
  />
</a>
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

Original concept and code for rendering heads: [MCRcortex](https://github.com/MCRcortex/)
