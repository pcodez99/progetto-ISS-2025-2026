class CombatLog:
    """
    MVP: semplice buffer. In Epic 9 lo sostituirete con UI/log pipeline.
    """
    def __init__(self):
        self.lines = []

    def add(self, line: str) -> None:
        self.lines.append(str(line))
