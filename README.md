# 🚀 SaC: Shoot and Capture

A strategic 2-player turn-based game of prediction, evolution, and tactical warfare.

## 📜 Game Rules

**Objective:** Be the first player to reach **16 Points**.

### 🎮 Core Mechanics
The game operates on a unique **Pick & Guess** system:

1. **The Pick:** One player is randomly assigned as the **Picker**. They secretly select a position (0-5).
2. **The Guess:** The opponent tries to guess that number.

### 🔄 Turn Outcomes
- **✅ Correct Guess:**
    - The turn flips immediately. The Guesser becomes the new Picker.

- **❌ Wrong Guess:**
    - The Picker gets a free tactical move on their chosen position.
    - **Action Constraint:** If the picked position is already **Captured** by the enemy, the Picker cannot act. The turn flips to the opponent to prevent wasted moves.

### ⚔️ Actions & Scoring
If the Guesser fails, the Picker performs one of these actions on the chosen position:

| Action | Description | Points Awarded |
| :--- | :--- | :---: |
| **SPAWN** | Place a generic Novice unit on an empty spot. | **0** |
| **EVOLVE** | Upgrade a Novice to a specialized unit (Fighter). | **+1** |
| **ATTACK** | Capture an enemy position using a Fighter unit. | **+3** |

---
*Built with Java Spring Boot & WebSockets.*
