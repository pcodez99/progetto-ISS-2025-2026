from src.model.items.item_ids import ItemIds

class VinaliaChoice:
    """
    One-shot choice: esattamente 1 tra wine/vinegar/marranzano.
    """
    def __init__(self):
        self.chosen_id = None

    def is_chosen(self) -> bool:
        return self.chosen_id is not None

    def choose(self, option: str) -> str:
        if self.is_chosen():
            raise ValueError("Choice already made")

        option = (option or "").lower().strip()
        
        # FIX: Usa nomi costanti italiani
        if option == "wine" or option == "vino eterno":
            self.chosen_id = ItemIds.VINO_ETERNO
        elif option == "vinegar" or option == "aceto madre":
            self.chosen_id = ItemIds.ACETO_MADRE
        elif option == "marranzano" or option == "marranzano d'oro":
            self.chosen_id = ItemIds.MARRANZANO_ORO
        else:
            # Fallback sicuro se l'indice non matcha
            print(f"Warning: Unknown Vinalia option '{option}'. Defaulting to Wine.")
            self.chosen_id = ItemIds.VINO_ETERNO

        return self.chosen_id