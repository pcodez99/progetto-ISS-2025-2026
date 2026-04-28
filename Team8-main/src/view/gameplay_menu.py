class GameplayMenu:
    def __init__(self, controller):
        self.controller = controller

    def inventory_button_press(self, player_index: int):
        player_items, capacity, count = self.controller.get_player_inventory(player_index)
        self.show_player_inventory(player_items, capacity, count)
        return player_items, capacity, count  # test purposes

    def show_player_inventory(self, player_items, capacity: int, count: int):
        self.controller.show_player_inventory(player_items, capacity, count)

    def abilities_button_press(self, player_index: int):
        player_abilities = self.controller.get_player_abilities(player_index)
        self.show_player_abilities(player_abilities)
        return player_abilities  # test purposes

    def show_player_abilities(self, player_abilities):
        self.controller.show_player_abilities(player_abilities)
