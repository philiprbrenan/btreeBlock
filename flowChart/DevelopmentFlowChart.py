from graphviz import Digraph

# Create a Digraph object for the flowchart
dot = Digraph(comment='Flowchart Example')

dot.node('B',    'The Btree algorithm in normal Java',      fontcolor='red', style='bold', fontsize='12')
dot.node('BS',   'BtreeStuck\nfixed key/value stack')
dot.node('BSS',  'BtreeStuckStatic\nreduced use of new')
dot.node('BSML', 'BtreeSML\nstatic bit memory')
dot.node('BSP',  'BtreeSP\ntransactional')
dot.node('BSA',  'BtreeSA\ntransaction in bit memory');
dot.node('BPA',  'BtreePA\npseudo assembler');
dot.node('BDM',  'BtreeDM\nroutable Verilog');
dot.node('RVL',  'The Btree algorithm in Routable Verilog', fontcolor='red', style='bold', fontsize='12')

dot.node('S',    'Stuck\nfixed key/value stack in normal Java')
dot.node('SS',   'StuckStatic\nreduced use of new')
dot.node('SSML', 'StuckSML\nstatic bit memory')
dot.node('SSP',  'StuckSP\ntransactional')
dot.node('SSA',  'StuckSA\ntransaction in bit memory')
dot.node('SPA',  'StuckPA\npseudo assembler')
dot.node('SDM',  'StuckDM\nroutable Verilog')

dot.node('MPA',  'MemoryLayoutPA\npseudo assembler')
dot.node('MDM',  'MemoryLayoutDM\nroutable Verilog')

dot.edge('B',    'BS')                                                          # Edges between nodes
dot.edge('BS',   'BSS')
dot.edge('BSS',  'BSML')
dot.edge('BSML', 'BSP')
dot.edge('BSP',  'BSA')
dot.edge('BSA',  'BPA')
dot.edge('BPA',  'BDM')
dot.edge('BDM',  'RVL')

dot.edge('S',    'SS')
dot.edge('SS',   'SSML')
dot.edge('SSML', 'SSP')
dot.edge('SSP',  'SSA')
dot.edge('SSA',  'SPA')
dot.edge('SPA',  'SDM')

dot.edge('MPA',  'MDM')

dot.edge('S',    'BS'   , arrowhead='diamond', color='blue')
dot.edge('SS',   'BSS'  , arrowhead='diamond', color='blue')
dot.edge('SSML', 'BSML' , arrowhead='diamond', color='blue')
dot.edge('SSP',  'BSP'  , arrowhead='diamond', color='blue')
dot.edge('SSA',  'BSA'  , arrowhead='diamond', color='blue')
dot.edge('SPA',  'BPA'  , arrowhead='diamond', color='blue')
dot.edge('SDM',  'BDM'  , arrowhead='diamond', color='blue')

dot.edge('MPA',  'SPA'  , arrowhead='diamond', color='blue')
dot.edge('MPA',  'BPA'  , arrowhead='diamond', color='blue')
dot.edge('MDM',  'SDM'  , arrowhead='diamond', color='blue')
dot.edge('MDM',  'BDM'  , arrowhead='diamond', color='blue')

dot.render('DevelopmentFlowChart', format='png', cleanup=True)                  # Display
dot.view('flowchart_output')
