from graphviz import Digraph

# Create a Digraph object for the flowchart
dot = Digraph(comment='Flowchart Example')

dot.node('B',    'Btree\nnormal java')                                          # Nodes
dot.node('BS',   'BtreeStuck\nfixed key/value stack')
dot.node('BSS',  'BtreeStuckStatic\nreduced use of new')
dot.node('BSML', 'BtreeSML\nstatic bit memory')

dot.node('S',    'Stuck\nfixed key/value stack in normal java')
dot.node('SS',   'StuckStatic\nredued use of new')
dot.node('SSML', 'StuckSML\nstatic bit memory')

dot.node('L',    'Layout\nmemory layout in normal java')
dot.node('M',    'Memory\nbit memory in normal java')

dot.edge('B',   'BS')                                                           # Edges between nodes
dot.edge('BS',  'BSS')
dot.edge('BSS', 'BSML')

dot.edge('S',    'SS')
dot.edge('SS',   'SSML')
dot.edge('SSML', 'BSML')

dot.render('DevelopmentFlowChart', format='png', cleanup=True)                  # Display
dot.view('flowchart_output')
