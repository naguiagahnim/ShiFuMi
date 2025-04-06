# Projet de pierre-feuille-ciseaux en Kotlin réalisé dans le cadre de mes études

## Stratégie du Bot : Analyse et Adaptation

Le bot examine attentivement les décisions antérieures du joueur. Il maintient un registre des sélections passées dans une liste nommée `playerHistory`. Cette liste sert de base pour identifier les schémas et les préférences du joueur.

En scrutant la liste `playerHistory`, le bot dénombre la fréquence de chaque option choisie par le joueur. Cette analyse révèle l'option favorite du joueur. Par exemple, si le joueur opte fréquemment pour "pierre", le bot détecte cette inclinaison.

Une fois la préférence du joueur établie, le bot adopte une riposte stratégique. Il sélectionne l'option qui triomphe sur le choix favori du joueur. Ainsi, si le joueur privilégie "pierre", le bot choisit "feuille", car "feuille" bat "pierre". De même, si le joueur favorise "feuille", le bot sélectionne "ciseaux", et ainsi de suite.

Si le jeu en est à son commencement, ou si les sélections du joueur sont uniformément distribuées sans préférence notable, le bot recourt à une sélection aléatoire. Ce comportement imprévisible maintient un élément de surprise et empêche le joueur de prévoir les actions du bot.

Le bot évolue en fonction des comportements du joueur, rendant ses actions plus astucieuses et ardues à contrer. En optant pour le hasard au début ou en cas d'indécision, le bot préserve un certain niveau d'incertitude. Cette tactique incite le joueur à une réflexion plus profonde sur ses propres sélections pour éviter d'être prévisible, enrichissant ainsi son expérience de jeu.