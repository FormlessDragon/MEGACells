## ![About][header_about]
MEGA Cells is an add-on for [Applied Energistics 2][ae2] providing higher tiers of storage, ranging in capacity from 1M to as high as 256M, similarly to add-ons of old such as Extra Cells 2 and its successors.

Unlike conventional add-ons in the same vein, MEGA does things quite differently, featuring its own dedicated progression line and components to further augment and challenge existing AE2 players' set-ups, while emphasising a distinct visual style with jet-black colour schemes.

This project is a fork of MEGA Cells for 1.21.1, based on [Applied Energistics 2 Supergiant](https://github.com/FormlessDragon/Applied-Energistics-2-Supergiant).

Need Java25.

## ![License][header_license]
All code is licensed under [LGPLv3][lgpl-v3], in adherence to the same license used by Applied Energistics 2 and with some code borrowed from AE2 itself.
All assets are licensed under [CC BY-NC-SA 3.0][by-nc-sa-3.0], in adherence to the same license used by AE2 and with most deriving from AE2's own assets.

## Maven

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    var mod_version = "v1.0.0"
    implementation "com.github.FormlessDragon:MEGACells:${mod_version}:dev"
}
```

<!-- Images -->
[logo]: https://raw.githubusercontent.com/62832/MEGACells/1.20/img/MEGACELLS.png
[badge_curseforge]: https://img.shields.io/badge/dynamic/json?color=e04e14&label=CurseForge&style=for-the-badge&query=downloads.total&url=https%3A%2F%2Fapi.cfwidget.com%2F622112&logo=curseforge
[badge_modrinth]: https://img.shields.io/modrinth/dt/jjuIRIVr?color=5da545&label=Modrinth&style=for-the-badge&logo=modrinth
[header_about]: https://raw.githubusercontent.com/62832/MEGACells/1.20/img/header_about.png
[header_features]: https://raw.githubusercontent.com/62832/MEGACells/1.20/img/header_features.png
[header_license]: https://raw.githubusercontent.com/62832/MEGACells/1.20/img/header_license.png

<!-- Links -->
[this]: https://github.com/62832/MEGACells
[curseforge]: https://www.curseforge.com/minecraft/mc-mods/mega-cells
[modrinth]: https://modrinth.com/mod/mega
[ae2]: https://github.com/AppliedEnergistics/Applied-Energistics-2
[appmek]: https://github.com/AppliedEnergistics/Applied-Mekanistics
[appbot]: https://github.com/ramidzkh/Applied-Botanics
[lgpl-v3]: https://www.gnu.org/licenses/lgpl-3.0.en.html
[by-nc-sa-3.0]: https://creativecommons.org/licenses/by-nc-sa/3.0/