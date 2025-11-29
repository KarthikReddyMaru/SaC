# 🚀 SaC: Shoot and Capture

A strategic 2-player turn-based game of prediction, tactics, and evolution.

## 📜 Game Rules

**Objective:** Be the first player to reach **16 Points**.

### 🎮 Core Mechanics
The game operates on a unique **Pick & Guess** system:

1. **The Pick:** One player is randomly assigned as the **Picker**. They secretly select a position (0-5).
2. **The Guess:** The opponent tries to guess that number.

### 🔄 Turn Outcomes
- **✅ Correct Guess:**
  - The turn flips immediately. The Guesser becomes the new Picker.
  - **Scoring:** The Guesser earns points based on the *Picker's Army Size* at that position.
    - **Formula:** `(Army Size) x (Unit Multiplier)`
    - **Multipliers:** Novice (1x) | Fighter (1.5x)

- **❌ Wrong Guess:**
  - The Picker earns a **Tactical Action** on their chosen position.
  - **Foul Move Penalty:** If the Picker selects a position that is already **Captured** by the enemy, they commit a Foul.
    - **Penalty:** -1 Point.
    - The turn flips to the opponent immediately.

### ⚔️ Tactical Actions & Scoring
If the Guesser fails, the Picker performs one of these actions on their chosen position.
*Note: Points are only awarded for aggressive plays (Kamikaze & Attack).*

| Action | Description | Points Awarded |
| :--- | :--- | :---: |
| **SPAWN** | Place a generic Novice unit on an empty spot. | **0** |
| **EVOLVE** | Upgrade a Novice to a specialized unit (Fighter). | **0** |
| **ATTACK** | Capture an enemy position using a Fighter unit. | **+3** |
| **KAMIKAZE** | Sacrifice a Novice unit to destroy an enemy. | **+1** |

---
*Built with Java Spring Boot & WebSockets.*
