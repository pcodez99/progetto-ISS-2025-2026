class ErrorScreen:
    def __init__(self, message: str):
        self.message = message

    def render(self):
        # placeholder: per ora stampa; in pygame diventa schermata
        print("A friendly error occurred:")
        print(self.message)
